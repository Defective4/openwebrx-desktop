package io.github.defective4.sdr.owrxdesktop;

import java.net.URI;
import java.util.Arrays;

import javax.swing.UIManager;

import com.formdev.flatlaf.FlatDarkLaf;

import io.github.defective4.sdr.owrxclient.client.OpenWebRXClient;
import io.github.defective4.sdr.owrxclient.event.OWRXAdapter;
import io.github.defective4.sdr.owrxclient.model.ReceiverProfile;
import io.github.defective4.sdr.owrxclient.model.ServerConfig;
import io.github.defective4.sdr.owrxdesktop.ui.ReceiverWindow;
import io.github.defective4.sdr.owrxdesktop.ui.component.WaterfallPanel;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
            ReceiverWindow window = new ReceiverWindow();
            window.setVisible(true);
            WaterfallPanel fftPanel = window.getPanel();
            fftPanel.setTuningStep((int) 50e3f);
            fftPanel.setCenterFrequency((int) 100e6);
            fftPanel.setScopeLower((int) -75e3f);
            fftPanel.setScopeUpper((int) 75e3f);
            fftPanel.setFFTDecimation(1);
            fftPanel.setTuningReady(true);

            OpenWebRXClient client = new OpenWebRXClient(URI.create("wss://radio.raspberry.local/ws/"));
            client.addListener(new OWRXAdapter() {

                @Override
                public void fftUpdated(float[] fft) {
                    fftPanel.drawFFT(fft);
                }

                @Override
                public void receiverProfilesUpdated(ReceiverProfile[] profiles) {
                    client.switchProfile(Arrays.stream(profiles)
                            .filter(profile -> profile.name().equals("RTL-SDR Broadcast FM 100-102 MHz")).findAny()
                            .get());
                }

                @Override
                public void serverConfigChanged(ServerConfig config) {
                    if (config.sampleRate() != null) fftPanel.setBandwidth(config.sampleRate());
                    if (config.tuningStep() != null) fftPanel.setTuningStep(config.tuningStep());
                    if (config.centerFrequency() != null) fftPanel.setCenterFrequency(config.centerFrequency());
                    if (config.startOffsetFrequency() != null) {
                        fftPanel.tune(config.startOffsetFrequency(), false);
                    }
                    if (config.startModulation() != null) {
                        int low = config.startModulation().getLowPass().orElse(-10000);
                        int high = config.startModulation().getHighPass().orElse(10000);

                        fftPanel.setScopeLower(low);
                        fftPanel.setScopeUpper(high);
                    }

                    fftPanel.setTuningReady(true);
                }
            });
            client.connect();

//            new Timer(true).scheduleAtFixedRate(new TimerTask() {
//                private float last;
//                private final float max = -30;
//                private final float min = -88;
//                private final int minMin = 120;
//                private boolean up;
//
//                {
//                    last = min;
//                }
//
//                @Override
//                public void run() {
//                    float[] fft = new float[8096];
//                    last = -minMin;
//                    for (int i = 0; i < fft.length; i++) {
//                        if (last >= max)
//                            up = false;
//                        else if (last <= -minMin) up = true;
//                        last += up ? 0.1 : -0.1;
//                        fft[i] = Math.max(min, last);
//                    }
//
//                    window.getPanel().drawFFT(fft);
//                }
//            }, 100, 100);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
