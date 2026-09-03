package io.github.defective4.sdr.owrxdesktop.application.integration.discord;

import java.text.ParseException;
import java.time.Instant;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

import de.jcm.discordgamesdk.Core;
import de.jcm.discordgamesdk.CreateParams;
import de.jcm.discordgamesdk.activity.Activity;
import de.jcm.discordgamesdk.activity.ActivityType;
import io.github.defective4.sdr.owrxclient.model.ReceiverDetails;
import io.github.defective4.sdr.owrxclient.model.ReceiverMode;
import io.github.defective4.sdr.owrxdesktop.RadioReceiver;
import io.github.defective4.sdr.owrxdesktop.ui.ApplicationWindow;
import io.github.defective4.sdr.owrxdesktop.ui.ReceiverWindow;
import io.github.defective4.sdr.owrxdesktop.ui.component.JFrequencySpinner.FrequencyFormatter;

public class DiscordPresence {
    private static final long APP_ID = 1544459542409117776L;
    private static final String DEFAULT_RECEIVER_NAME = "<No name>";
    private static final String LOGO_KEY = "logo";
    private Activity activity;
    private Core core;

    private boolean enabled;
    private final FrequencyFormatter frequencyFormatter = new FrequencyFormatter();

    private long lastPresenceUpdate = 0;

    private final Object lock = new Object();
    private final long timestamp = System.currentTimeMillis();
    private final ApplicationWindow window;

    public DiscordPresence(ApplicationWindow window) {
        this.window = window;
        setEnabled(window.getUserStorage().getApplicationSettings().isEnableDiscordPresence());
        new Timer(true).scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                synchronized (lock) {
                    setActivity();
                }
            }
        }, 0, 5000);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled && core != null) {
            core.close();
            core = null;
        } else if (enabled) {
            open();
        }
    }

    public void updatePresence() {
        if (!enabled) return;
        Activity activity = new Activity();
        activity.setInstance(true);
        activity.setType(ActivityType.PLAYING);
        activity.setDetails("In server list");
        activity.timestamps().setStart(Instant.ofEpochMilli(timestamp));
        activity.assets().setSmallImage(null);

        if (window.getReceiver().isPresent()) {
            RadioReceiver rx = window.getReceiver().get();
            Optional<ReceiverDetails> details = rx.getReceiverDetails();
            String name = details.map(ReceiverDetails::receiverName).orElse(DEFAULT_RECEIVER_NAME);
            if (name.length() > 128) name = name.substring(0, 128);
            activity.setDetails(name);
            activity.assets().setSmallImage(rx.getHttpURL() + "static/gfx/openwebrx-avatar.png");
            if (rx.isConnected()) {
                ReceiverWindow rxWindow = rx.getRxWindow();
                String freq;
                try {
                    freq = frequencyFormatter.valueToString(rxWindow.getCenterFrequency() + rxWindow.getOffset());
                } catch (ParseException e) {
                    freq = "%s Hz".formatted(rxWindow.getCenterFrequency() + rxWindow.getOffset());
                }
                ReceiverMode mode = rxWindow.getSecondaryMode().orElse(rxWindow.getPrimaryMode());
                String fmt = "%s %s".formatted(freq, (mode == null ? ReceiverMode.EMPTY : mode).name());
                activity.setState(fmt);
            } else {
                activity.setState("Connecting...");
            }
        } else if (window.getAppState().changingSettings) {
            activity.setState("In settings");
        } else {
            activity.setState(null);
        }

        activity.assets().setLargeImage(LOGO_KEY);
        activity.assets().setLargeText("OpenWebRX Desktop");

        synchronized (lock) {
            this.activity = activity;
            if (System.currentTimeMillis() - lastPresenceUpdate > 5000) {
                setActivity();
            }
        }
    }

    private void open() {
        try {
            CreateParams params = new CreateParams();
            params.setFlags(CreateParams.getDefaultFlags());
            params.setClientID(APP_ID);
            core = new Core(params);
            enabled = true;
        } catch (Exception e) {
            e.printStackTrace();
            setEnabled(false);
        }
    }

    private void setActivity() {
        if (!enabled) return;
        if (activity == null) return;
        core.activityManager().updateActivity(activity);
        activity = null;
        lastPresenceUpdate = System.currentTimeMillis();
    }
}
