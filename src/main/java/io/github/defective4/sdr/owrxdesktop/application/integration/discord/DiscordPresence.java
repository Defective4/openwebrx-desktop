package io.github.defective4.sdr.owrxdesktop.application.integration.discord;

import java.text.ParseException;
import java.time.Instant;

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
    private final ActivityManager actManager;
    private final Core core;
    private final FrequencyFormatter frequencyFormatter = new FrequencyFormatter();

    private final long timestamp = System.currentTimeMillis();
    private final ApplicationWindow window;

    public DiscordPresence(ApplicationWindow window) {
        this.window = window;
        CreateParams params = new CreateParams();
        params.setFlags(CreateParams.getDefaultFlags());
        params.setClientID(APP_ID);
        core = new Core(params);
        actManager = core.activityManager();
    }

    public void updatePresence() {
        Activity act = new Activity();
        act.setInstance(true);
        act.setType(ActivityType.PLAYING);
        act.setDetails("In server list");
        act.timestamps().setStart(Instant.ofEpochMilli(timestamp));

        if (window.getReceiver().isPresent()) {
            RadioReceiver rx = window.getReceiver().get();
            act.setDetails(rx.getReceiverDetails().map(ReceiverDetails::receiverName).orElse(DEFAULT_RECEIVER_NAME));
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
                act.setState(fmt);
            } else {
                act.setState("Connecting...");
            }
        } else if (window.getAppState().changingSettings) {
            act.setState("In settings");
        } else {
            act.setState(null);
        }

        act.assets().setLargeImage(LOGO_KEY);
        act.assets().setLargeText("OpenWebRX Desktop");

        actManager.updateActivity(act);
    }
}
