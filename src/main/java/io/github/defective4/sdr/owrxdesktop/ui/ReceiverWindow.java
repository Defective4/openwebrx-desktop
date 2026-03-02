package io.github.defective4.sdr.owrxdesktop.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Objects;

import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import io.github.defective4.sdr.owrxclient.model.WaterfallLevels;
import io.github.defective4.sdr.owrxdesktop.bandplan.Bandplan;
import io.github.defective4.sdr.owrxdesktop.ui.component.FFTPanel;
import io.github.defective4.sdr.owrxdesktop.ui.component.TuneablePanel;
import io.github.defective4.sdr.owrxdesktop.ui.component.WaterfallPanel;
import io.github.defective4.sdr.owrxdesktop.ui.event.TuningAdapter;

public class ReceiverWindow extends JFrame {

    private final Bandplan bandplan = new Bandplan();

    private final FFTPanel fftPanel;

    private final JRadioButton ftlAuto = new JRadioButton("Auto");

    private final JRadioButton ftlServer = new JRadioButton("Server");
    private long lastFFTDraw;

    private int maxFPS = -1;

    private float minFFT, maxFFT;

    private WaterfallLevels serverLevels = new WaterfallLevels(-88, -20);

    private final WaterfallPanel waterfallPanel;

    public ReceiverWindow() {
        resetAutoFFT();
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
            featPanel.setBorder(new TitledBorder(null, "Features", TitledBorder.LEADING, TitledBorder.TOP, null, null));
            fftCtlPanel.add(featPanel);
            featPanel.setLayout(new BoxLayout(featPanel, BoxLayout.Y_AXIS));
            JPanel panel = new JPanel();
            panel.setAlignmentX(Component.LEFT_ALIGNMENT);
            featPanel.add(panel);
            panel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));

            JCheckBox bandplanCheck = new JCheckBox("Bandplan");
            panel.add(bandplanCheck);
            bandplanCheck.setSelected(true);

            bandplanCheck.addActionListener(e -> { fftPanel.setShowBandplan(bandplanCheck.isSelected()); });

            confirmComponentState(bandplanCheck);

            JCheckBox maxDrawCheck = new JCheckBox("Draw max");
            panel.add(maxDrawCheck);

            JPanel levelsPanel = new JPanel();
            compactPanel(levelsPanel);
            levelsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            levelsPanel.setBorder(new TitledBorder(null, "Levels", TitledBorder.LEADING, TitledBorder.TOP, null, null));
            fftCtlPanel.add(levelsPanel);
            levelsPanel.setLayout(new BoxLayout(levelsPanel, BoxLayout.Y_AXIS));

            JPanel fftLevelModePanel = new JPanel();
            FlowLayout flowLayout = (FlowLayout) fftLevelModePanel.getLayout();
            flowLayout.setAlignment(FlowLayout.LEFT);
            fftLevelModePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            levelsPanel.add(fftLevelModePanel);

            ftlServer.setSelected(true);
            fftLevelModePanel.add(ftlServer);

            fftLevelModePanel.add(ftlAuto);

            JRadioButton ftlManual = new JRadioButton("Manual");
            fftLevelModePanel.add(ftlManual);

            ButtonGroup ftl = new ButtonGroup();
            ftl.add(ftlServer);
            ftl.add(ftlAuto);
            ftl.add(ftlManual);

            levelsPanel.add(new JLabel("Min"));

            JPanel minPanel = new JPanel();
            minPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            levelsPanel.add(minPanel);
            minPanel.setLayout(new BoxLayout(minPanel, BoxLayout.X_AXIS));

            JLabel minField = new JLabel();

            JSlider minSlider = new JSlider();
            minSlider.setMinimum(-200);
            minSlider.setMaximum(100);
            minSlider.setValue(-88);
            minPanel.add(minSlider);
            minPanel.add(minField);

            levelsPanel.add(new JLabel("Max"));

            JPanel maxPanel = new JPanel();
            maxPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            levelsPanel.add(maxPanel);
            maxPanel.setLayout(new BoxLayout(maxPanel, BoxLayout.X_AXIS));

            JLabel maxField = new JLabel();

            JSlider maxSlider = new JSlider();
            maxSlider.setMinimum(-200);
            maxSlider.setMaximum(100);
            maxSlider.setValue(-20);
            maxPanel.add(maxSlider);
            maxPanel.add(maxField);

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
            JCheckBox colorMixingCheck = new JCheckBox("Dynamic color mixing");
            colorMixingCheck.setSelected(true);
            stylePanel.add(colorMixingCheck);

            JButton btnResetMax = new JButton("Reset max");
            featPanel.add(btnResetMax);

            minSlider.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    int val = minSlider.getValue();
                    if (val >= maxSlider.getValue()) maxSlider.setValue(val + 1);
                    minField.setText(Integer.toString(val));
                    maxField.setText(Integer.toString(maxSlider.getValue()));

                    setFFTMax(maxSlider.getValue());
                    setFFTMin(val);

                    fftPanel.repaint();
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

                    fftPanel.repaint();
                }
            });

            featPanel.add(new JLabel(" "));

            JPanel maxFpsPanel = new JPanel();
            maxFpsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            featPanel.add(maxFpsPanel);
            maxFpsPanel.setLayout(new BoxLayout(maxFpsPanel, BoxLayout.X_AXIS));

            maxFpsPanel.add(new JLabel("Max FPS: "));

            JLabel maxFpsLabel = new JLabel("-");
            maxFpsPanel.add(maxFpsLabel);

            JSlider maxFpsSlider = new JSlider();
            featPanel.add(maxFpsSlider);
            maxFpsSlider.setMaximum(61);
            maxFpsSlider.setValue(maxFpsSlider.getMaximum());
            maxFpsSlider.setAlignmentX(Component.LEFT_ALIGNMENT);

            maxFpsSlider.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    int val = maxFpsSlider.getValue();
                    if (val >= maxFpsSlider.getMaximum()) val = -1;
                    maxFPS = val;
                    maxFpsLabel.setText(val == -1 ? "Unlimited" : Integer.toString(val));
                }
            });

            ActionListener ftlListener = e -> {
                minSlider.setEnabled(ftlManual.isSelected());
                maxSlider.setEnabled(ftlManual.isSelected());

                if (ftlServer.isSelected() || ftlAuto.isSelected()) {
                    setFFTMax(serverLevels.max());
                    setFFTMin(serverLevels.min());

                    resetAutoFFT();
                } else {
                    confirmComponentState(minSlider);
                    confirmComponentState(maxSlider);
                }
            };

            ftlManual.addActionListener(ftlListener);
            ftlAuto.addActionListener(ftlListener);
            ftlServer.addActionListener(ftlListener);

            confirmComponentState(maxFpsSlider);
            confirmComponentState(solidCheck);
            confirmComponentState(colorMixingCheck);
            confirmComponentState(ftlManual);

            confirmComponentState(minSlider);
            confirmComponentState(maxSlider);

            colorMixingCheck.addActionListener(e -> waterfallPanel.setColorMixing(colorMixingCheck.isSelected()));
            solidCheck.addActionListener(e -> fftPanel.setSolid(solidCheck.isSelected()));

            maxDrawCheck.addActionListener(e -> {
                fftPanel.setDrawMaxValues(maxDrawCheck.isSelected());
                btnResetMax.setEnabled(maxDrawCheck.isSelected());
                fftPanel.repaint();
            });

            btnResetMax.addActionListener(e -> fftPanel.resetMaxFFT());

            confirmComponentState(maxDrawCheck);
        }
    }

    public void drawFFT(float[] fft, int offset) {
        if (maxFPS > 0) {
            if (System.currentTimeMillis() - lastFFTDraw < 1000 / maxFPS) return;
            lastFFTDraw = System.currentTimeMillis();
        } else if (maxFPS == 0) return;
        for (TuneablePanel fftPanel : getPanels()) fftPanel.drawFFT(fft, offset);
        if (ftlAuto.isSelected()) {
            for (int i = offset; i < fft.length; i++) {
                float f = fft[i];
                if (f < minFFT) {
                    minFFT = f;
                    setFFTMin(f);
                }
            }
            for (int i = offset; i < fft.length; i++) {
                float f = fft[i];
                if (f > maxFFT) {
                    maxFFT = f;
                    setFFTMax(f);
                }
            }
        }
    }

    public Bandplan getBandplan() {
        return bandplan;
    }

    public FFTPanel getFftPanel() {
        return fftPanel;
    }

    public TuneablePanel[] getPanels() {
        return new TuneablePanel[] { waterfallPanel, fftPanel };
    }

    public WaterfallLevels getServerLevels() {
        return serverLevels;
    }

    public void resetAutoFFT() {
        minFFT = Integer.MAX_VALUE;
        maxFFT = Integer.MIN_VALUE;
    }

    public void setBandwidth(int bandwidth) {
        for (TuneablePanel fftPanel : getPanels()) fftPanel.setBandwidth(bandwidth);
    }

    public void setCenterFrequency(int centerFrequency) {
        for (TuneablePanel fftPanel : getPanels()) fftPanel.setCenterFrequency(centerFrequency);
    }

    public void setFFTMax(float f) {
        for (TuneablePanel fftPanel : getPanels()) fftPanel.setFFTMax(f);
    }

    public void setFFTMin(float f) {
        for (TuneablePanel fftPanel : getPanels()) fftPanel.setFFTMin(f);
    }

    public void setScopeLower(int scopeLower) {
        for (TuneablePanel fftPanel : getPanels()) fftPanel.setScopeLower(scopeLower);
    }

    public void setScopeUpper(int scopeUpper) {
        for (TuneablePanel fftPanel : getPanels()) fftPanel.setScopeUpper(scopeUpper);
    }

    public void setServerLevels(WaterfallLevels serverLevels) {
        this.serverLevels = Objects.requireNonNull(serverLevels);
        if (ftlServer.isSelected()) {
            setFFTMax(serverLevels.max());
            setFFTMin(serverLevels.min());
        }
        repaint();
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

    private static void confirmComponentState(JSlider component) {
        for (ChangeListener ls : component.getChangeListeners()) ls.stateChanged(null);
    }

}
