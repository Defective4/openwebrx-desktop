package io.github.defective4.sdr.owrxdesktop.ui;

import java.awt.Color;
import java.util.Arrays;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import io.github.defective4.sdr.owrxdesktop.ui.component.FFTPanel;
import io.github.defective4.sdr.owrxdesktop.ui.component.TuneablePanel;
import io.github.defective4.sdr.owrxdesktop.ui.component.WaterfallPanel;
import io.github.defective4.sdr.owrxdesktop.ui.event.TuningAdapter;

public class ReceiverWindow extends JFrame {

    private final FFTPanel fftPanel;
    private final WaterfallPanel waterfallPanel;

    public ReceiverWindow() {
        setBounds(100, 100, 768, 468);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        JSplitPane splitPane = new JSplitPane();
        splitPane.setResizeWeight(0.75);
        getContentPane().add(splitPane);

        JPanel controlPanel = new JPanel();
        splitPane.setRightComponent(controlPanel);

        JSplitPane fftPane = new JSplitPane();
        fftPane.setResizeWeight(0.75);
        fftPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        splitPane.setLeftComponent(fftPane);

        fftPanel = new FFTPanel();
        fftPane.setLeftComponent(fftPanel);

        waterfallPanel = new WaterfallPanel();
        waterfallPanel.setTheme(Arrays.stream(
                "0x000020, 0x000030, 0x000050, 0x000091, 0x1E90FF, 0xFFFFFF, 0xFFFF00, 0xFE6D16, 0xFF0000, 0xC60000, 0x9F0000, 0x750000, 0x4A0000"
                        .replace("0x", "#").split(", "))
                .map(Color::decode).toArray(Color[]::new));

        fftPane.setRightComponent(waterfallPanel);

        waterfallPanel.addListener(new TuningAdapter() {
            @Override
            public void tuned(int offset) {
                fftPanel.tune(offset, false);
            }
        });

        fftPanel.addListener(new TuningAdapter() {
            @Override
            public void tuned(int offset) {
                waterfallPanel.tune(offset, false);
            }

            @Override
            public void zoomChanged(int x, int width) {
                waterfallPanel.setBounds(x, waterfallPanel.getY(), width, getHeight());
                waterfallPanel.invalidate();
            }

        });
    }

    public void drawFFT(float[] fft) {
        for (TuneablePanel fftPanel : getPanels()) fftPanel.drawFFT(fft);
    }

    public void setBandwidth(int bandwidth) {
        for (TuneablePanel fftPanel : getPanels()) fftPanel.setBandwidth(bandwidth);
    }

    public void setCenterFrequency(int centerFrequency) {
        for (TuneablePanel fftPanel : getPanels()) fftPanel.setCenterFrequency(centerFrequency);
    }

    public void setScopeLower(int scopeLower) {
        for (TuneablePanel fftPanel : getPanels()) fftPanel.setScopeLower(scopeLower);
    }

    public void setScopeUpper(int scopeUpper) {
        fftPanel.setScopeUpper(scopeUpper);
    }

    public void setSolid(boolean solid) {
        fftPanel.setSolid(solid);
    }

    public void setTuningReady(boolean tuningReady) {
        for (TuneablePanel fftPanel : getPanels()) fftPanel.setTuningReady(tuningReady);
    }

    public void setTuningStep(int tuningStep) {
        for (TuneablePanel fftPanel : getPanels()) fftPanel.setTuningStep(tuningStep);
    }

    public void setWaterfallTheme(Color[] theme) {
        waterfallPanel.setTheme(theme);
    }

    public void tune(int offset) {
        for (TuneablePanel fftPanel : getPanels()) fftPanel.tune(offset);
    }

    public void tune(int offset, boolean fireEvents) {
        for (TuneablePanel fftPanel : getPanels()) fftPanel.tune(offset, fireEvents);
    }

    private TuneablePanel[] getPanels() {
        return new TuneablePanel[] { waterfallPanel, fftPanel };
    }

}
