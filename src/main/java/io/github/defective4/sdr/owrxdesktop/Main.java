package io.github.defective4.sdr.owrxdesktop;

import javax.swing.UIManager;

import com.formdev.flatlaf.FlatDarkLaf;

import io.github.defective4.sdr.owrxdesktop.ui.ReceiverWindow;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
            ReceiverWindow window = new ReceiverWindow();
            window.setVisible(true);
//            OpenWebRXClient client = new OpenWebRXClient(URI.create("wss://radio.raspberry.local/ws/"));
//            client.addListener(new OWRXAdapter() {
//
//                @Override
//                public void fftUpdated(float[] fft) {
//                    float[] converted = new float[fft.length];
//                    for (int i = 0; i < fft.length; i++) {
//                        float f = 100 + fft[i];
//                        converted[i] = f;
//                    }
//                    window.getFFTPanel().setFFT(converted);
//                }
//
//                @Override
//                public void receiverProfilesUpdated(ReceiverProfile[] profiles) {
//                    client.switchProfile(Arrays.stream(profiles)
//                            .filter(profile -> profile.name().equals("RTL-SDR Broadcast FM 100-102 MHz")).findAny()
//                            .get());
//                }
//            });
//            client.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
