package io.github.defective4.sdr.owrxdesktop.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.util.Arrays;

import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import io.github.defective4.sdr.owrxdesktop.bandplan.Bandplan;
import io.github.defective4.sdr.owrxdesktop.ui.component.FFTPanel;
import io.github.defective4.sdr.owrxdesktop.ui.component.TuneablePanel;
import io.github.defective4.sdr.owrxdesktop.ui.component.WaterfallPanel;
import io.github.defective4.sdr.owrxdesktop.ui.event.TuningAdapter;

public class ReceiverWindow extends JFrame {

    private final Bandplan bandplan = new Bandplan();

    private final FFTPanel fftPanel;

    private final WaterfallPanel waterfallPanel;

    public ReceiverWindow() {
        setBounds(100, 100, 768, 468);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        JSplitPane splitPane = new JSplitPane();
        splitPane.setResizeWeight(1);
        getContentPane().add(splitPane);

        JPanel controlPanel = new JPanel();
        splitPane.setRightComponent(controlPanel);
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.X_AXIS));

        JTabbedPane controlTabs = new JTabbedPane(JTabbedPane.TOP);
        controlPanel.add(controlTabs);

        {
            JSplitPane fftPane = new JSplitPane();
            fftPane.setResizeWeight(0.75);
            fftPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
            splitPane.setLeftComponent(fftPane);

            fftPanel = new FFTPanel(bandplan);
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

        {
            JPanel fftCtlPanel = new JPanel();
            controlTabs.addTab("FFT", null, fftCtlPanel, null);
            fftCtlPanel.setLayout(new BoxLayout(fftCtlPanel, BoxLayout.Y_AXIS));

            JPanel featPanel = new JPanel();
            featPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            compactPanel(featPanel);
            FlowLayout flowLayout = (FlowLayout) featPanel.getLayout();
            flowLayout.setAlignment(FlowLayout.LEFT);
            featPanel.setBorder(new TitledBorder(null, "Features", TitledBorder.LEADING, TitledBorder.TOP, null, null));
            fftCtlPanel.add(featPanel);

            JCheckBox bandplanCheck = new JCheckBox("Bandplan");
            bandplanCheck.setSelected(true);
            featPanel.add(bandplanCheck);

            JPanel levelsPanel = new JPanel();
            compactPanel(levelsPanel);
            levelsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            levelsPanel.setBorder(new TitledBorder(null, "Levels", TitledBorder.LEADING, TitledBorder.TOP, null, null));
            fftCtlPanel.add(levelsPanel);
            levelsPanel.setLayout(new BoxLayout(levelsPanel, BoxLayout.Y_AXIS));

            JCheckBox autoCheck = new JCheckBox("Auto");
            autoCheck.setSelected(true);
            levelsPanel.add(autoCheck);

            levelsPanel.add(new JLabel("Min"));

            JPanel minPanel = new JPanel();
            minPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            levelsPanel.add(minPanel);
            minPanel.setLayout(new BoxLayout(minPanel, BoxLayout.X_AXIS));

            JTextField minField = new JTextField();
            minField.setEditable(false);
            minPanel.add(minField);
            minField.setColumns(3);

            JSlider minSlider = new JSlider();
            minSlider.setMinimum(-100);
            minSlider.setMaximum(0);
            minSlider.setValue(-88);
            minPanel.add(minSlider);

            levelsPanel.add(new JLabel("Max"));

            JPanel maxPanel = new JPanel();
            maxPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            levelsPanel.add(maxPanel);
            maxPanel.setLayout(new BoxLayout(maxPanel, BoxLayout.X_AXIS));

            JTextField maxField = new JTextField();
            maxField.setEditable(false);
            maxPanel.add(maxField);
            maxField.setColumns(3);

            JSlider maxSlider = new JSlider();
            maxSlider.setMinimum(-100);
            maxSlider.setMaximum(0);
            maxSlider.setValue(-20);
            maxPanel.add(maxSlider);

            JPanel stylePanel = new JPanel();
            compactPanel(stylePanel);
            FlowLayout flowLayout_1 = (FlowLayout) stylePanel.getLayout();
            flowLayout_1.setAlignment(FlowLayout.LEFT);
            stylePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            stylePanel.setBorder(new TitledBorder(null, "Style", TitledBorder.LEADING, TitledBorder.TOP, null, null));
            fftCtlPanel.add(stylePanel);

            JCheckBox solidCheck = new JCheckBox("Solid");
            stylePanel.add(solidCheck);

            JPanel filler = new JPanel();
            filler.setAlignmentX(Component.LEFT_ALIGNMENT);
            fftCtlPanel.add(filler);

            bandplanCheck.addActionListener(e -> { fftPanel.setShowBandplan(bandplanCheck.isSelected()); });

            autoCheck.addActionListener(e -> {
                boolean enabled = autoCheck.isSelected();
                minSlider.setEnabled(!enabled);
                maxSlider.setEnabled(!enabled);

                minField.setText(enabled ? "Auto" : Integer.toString(minSlider.getValue()));
                maxField.setText(enabled ? "Auto" : Integer.toString(maxSlider.getValue()));
            });

            minSlider.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    int val = minSlider.getValue();
                    if (val >= maxSlider.getValue()) maxSlider.setValue(val + 1);
                    minField.setText(Integer.toString(val));
                    maxField.setText(Integer.toString(maxSlider.getValue()));

                    setFFTMax(maxSlider.getValue());
                    setFFTMin(val);
                }
            });

            maxSlider.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    int val = maxSlider.getValue();
                    if (val <= minSlider.getValue()) minSlider.setValue(val - 1);
                    maxField.setText(Integer.toString(val));
                    minField.setText(Integer.toString(minSlider.getValue()));

                    setFFTMin(minSlider.getValue());
                    setFFTMax(val);
                }
            });

            solidCheck.addActionListener(e -> fftPanel.setSolid(solidCheck.isSelected()));

            confirmComponentState(bandplanCheck);
            confirmComponentState(autoCheck);
            confirmComponentState(solidCheck);
        }
    }

    public void drawFFT(float[] fft, int offset) {
        for (TuneablePanel fftPanel : getPanels()) fftPanel.drawFFT(fft, offset);
    }

    public Bandplan getBandplan() {
        return bandplan;
    }

    public TuneablePanel[] getPanels() {
        return new TuneablePanel[] { waterfallPanel, fftPanel };
    }

    public void setBandwidth(int bandwidth) {
        for (TuneablePanel fftPanel : getPanels()) fftPanel.setBandwidth(bandwidth);
    }

    public void setCenterFrequency(int centerFrequency) {
        for (TuneablePanel fftPanel : getPanels()) fftPanel.setCenterFrequency(centerFrequency);
    }

    public void setFFTMax(int max) {
        for (TuneablePanel fftPanel : getPanels()) fftPanel.setFFTMax(max);
    }

    public void setFFTMin(int min) {
        for (TuneablePanel fftPanel : getPanels()) fftPanel.setFFTMin(min);
    }

    public void setScopeLower(int scopeLower) {
        for (TuneablePanel fftPanel : getPanels()) fftPanel.setScopeLower(scopeLower);
    }

    public void setScopeUpper(int scopeUpper) {
        for (TuneablePanel fftPanel : getPanels()) fftPanel.setScopeUpper(scopeUpper);
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

    public void updateBandplan() {
        fftPanel.updateVisibleBands();
    }

    private static void compactPanel(JPanel featPanel) {
        featPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 0));
    }

    private static void confirmComponentState(AbstractButton component) {
        for (ActionListener ls : component.getActionListeners()) ls.actionPerformed(null);
    }

}
