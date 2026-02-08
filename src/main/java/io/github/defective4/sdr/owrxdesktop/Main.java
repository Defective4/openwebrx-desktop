package io.github.defective4.sdr.owrxdesktop;

import java.util.Timer;
import java.util.TimerTask;

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
            FFTPanel panel = window.getPanel();
            panel.setTuningReady(true);
            panel.setTuningStep((int) 50e3f);
            panel.setCenterFrequency((int) 100e6);
            panel.setScopeLower((int) -75e3f);
            panel.setScopeUpper((int) 75e3f);

            new Timer(true).scheduleAtFixedRate(new TimerTask() {
                private float last;
                private final float max = -30;
                private final float min = -88;
                private final int minMin = 120;
                private boolean up;

                {
                    last = min;
                }

                @Override
                public void run() {
                    float[] fft = new float[8096];
                    last = -minMin;
                    for (int i = 0; i < fft.length; i++) {
                        if (last >= max)
                            up = false;
                        else if (last <= -minMin) up = true;
                        last += up ? 0.1 : -0.1;
                        fft[i] = Math.max(min, last);
                    }

                    window.getPanel().drawFFT(fft);
                }
            }, 100, 100);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
