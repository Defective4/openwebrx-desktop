package io.github.defective4.sdr.owrxdesktop;

import javax.swing.UIManager;

import com.formdev.flatlaf.FlatDarkLaf;

import io.github.defective4.sdr.owrxdesktop.ui.ReceiverWindow;
import io.github.defective4.sdr.owrxdesktop.ui.component.FFTPanel;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
            ReceiverWindow window = new ReceiverWindow();
            window.setVisible(true);
            FFTPanel fftPanel = window.getFFTPanel();
            fftPanel.setCenterFrequency((int) 100e6f);
            fftPanel.setBandwidth(960000);
            fftPanel.setScopeLower((int) -75e3f);
            fftPanel.setScopeUpper((int) 75e3f);
            fftPanel.setTuningStep((int) 50e3f);
            fftPanel.setFFTMin(-88);
            fftPanel.setFFTMax(-20);

            fftPanel.setTuningReady(true);
//            OpenWebRXClient client = new OpenWebRXClient(URI.create("wss://radio.raspberry.local/ws/"));
//            client.addListener(new OWRXAdapter() {
//
//                @Override
//                public void fftUpdated(float[] fft) {
//                    window.getFFTPanel().drawFFT(fft);
//                }
//
//                @Override
//                public void receiverProfilesUpdated(ReceiverProfile[] profiles) {
//                    client.switchProfile(Arrays.stream(profiles)
//                            .filter(profile -> profile.name().equals("RTL-SDR Broadcast FM 100-102 MHz")).findAny()
//                            .get());
//                }
//
//                @Override
//                public void serverConfigChanged(ServerConfig config) {
//                    if (config.sampleRate() != null) fftPanel.setBandwidth(config.sampleRate());
//                    if (config.tuningStep() != null) fftPanel.setTuningStep(config.tuningStep());
//                    if (config.centerFrequency() != null) fftPanel.setCenterFrequency(config.centerFrequency());
//                    if (config.startOffsetFrequency() != null) {
//                        fftPanel.tune(config.startOffsetFrequency(), false);
//                    }
//                    if(config.startModulation()!=null) {
//                        int low = config.startModulation().getLowPass().orElse(-10000);
//                        int high = config.startModulation().getHighPass().orElse(10000);
//
//                        fftPanel.setScopeLower(low);
//                        fftPanel.setScopeUpper(high);
//                    }
//
//                    if(config.waterfallLevels()!=null) {
//                        WaterfallLevels levels = config.waterfallLevels();
//                        fftPanel.setFFTMax(levels.max());
//                        fftPanel.setFFTMin(levels.min());
//                    }
//
//
//                    fftPanel.setTuningReady(true);
//                }
//            });
//            client.connect();
        } catch (

        Exception e) {
            e.printStackTrace();
        }
    }
}
