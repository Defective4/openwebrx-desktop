package io.github.defective4.sdr.owrxdesktop.application.integration.discord;

import java.text.ParseException;
import java.time.Instant;
import java.util.Timer;
import java.util.TimerTask;

import de.jcm.discordgamesdk.ActivityManager;
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
    private final ActivityManager actManager;
    private final Core core;

    private final FrequencyFormatter frequencyFormatter = new FrequencyFormatter();
    private long lastPresenceUpdate = 0;

    private final Object lock = new Object();

    private final long timestamp = System.currentTimeMillis();
    private final ApplicationWindow window;
    public DiscordPresence(ApplicationWindow window) {
        this.window = window;
        CreateParams params = new CreateParams();
        params.setFlags(CreateParams.getDefaultFlags());
        params.setClientID(APP_ID);
        core = new Core(params);
        actManager = core.activityManager();
        new Timer(true).scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                synchronized (lock) {
                    setActivity();
                }
            }
        }, 0, 5000);
    }

    public void updatePresence() {
        Activity activity = new Activity();
        activity.setInstance(true);
        activity.setType(ActivityType.PLAYING);
        activity.setDetails("In server list");
        activity.timestamps().setStart(Instant.ofEpochMilli(timestamp));

        if (window.getReceiver().isPresent()) {
            RadioReceiver rx = window.getReceiver().get();
            activity.setDetails(
                    rx.getReceiverDetails().map(ReceiverDetails::receiverName).orElse(DEFAULT_RECEIVER_NAME));
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

    private void setActivity() {
        if (activity == null) return;
        actManager.updateActivity(activity);
        activity = null;
        lastPresenceUpdate = System.currentTimeMillis();
    }
}
