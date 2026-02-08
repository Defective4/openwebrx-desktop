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

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
            ReceiverWindow rxWindow = new ReceiverWindow();
            rxWindow.setVisible(true);
            rxWindow.setTuningStep((int) 50e3f);
            rxWindow.setCenterFrequency((int) 100e6);
            rxWindow.setScopeLower((int) -75e3f);
            rxWindow.setScopeUpper((int) 75e3f);
            rxWindow.setTuningReady(true);

            OpenWebRXClient client = new OpenWebRXClient(URI.create("wss://radio.raspberry.local/ws/"));
            client.addListener(new OWRXAdapter() {

                @Override
                public void fftUpdated(float[] fft) {
                    rxWindow.drawFFT(fft);
                }

                @Override
                public void receiverProfilesUpdated(ReceiverProfile[] profiles) {
                    client.switchProfile(Arrays.stream(profiles)
                            .filter(profile -> profile.name().equals("RTL-SDR Broadcast FM 100-102 MHz")).findAny()
                            .get());
                }

                @Override
                public void serverConfigChanged(ServerConfig config) {
                    if (config.sampleRate() != null) rxWindow.setBandwidth(config.sampleRate());
                    if (config.tuningStep() != null) rxWindow.setTuningStep(config.tuningStep());
                    if (config.centerFrequency() != null) rxWindow.setCenterFrequency(config.centerFrequency());
                    if (config.startOffsetFrequency() != null) {
                        rxWindow.tune(config.startOffsetFrequency(), false);
                    }
                    if (config.startModulation() != null) {
                        int low = config.startModulation().getLowPass().orElse(-10000);
                        int high = config.startModulation().getHighPass().orElse(10000);

                        rxWindow.setScopeLower(low);
                        rxWindow.setScopeUpper(high);
                    }
                    if(config.waterfallColors()!=null) {
                        rxWindow.setWaterfallTheme(config.mappedWaterfallColors());
                    }

                    rxWindow.setTuningReady(true);
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
//                    long start = System.nanoTime();
//                    fftPanel.drawFFT(fft);
//                }
//            }, 100, 100);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
