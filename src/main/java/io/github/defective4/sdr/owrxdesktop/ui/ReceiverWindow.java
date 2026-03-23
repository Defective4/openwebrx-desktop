package io.github.defective4.sdr.owrxdesktop.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JSpinner.NumberEditor;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import io.github.defective4.sdr.owrxclient.model.Bandpass;
import io.github.defective4.sdr.owrxclient.model.ReceiverMode;
import io.github.defective4.sdr.owrxclient.model.ReceiverProfile;
import io.github.defective4.sdr.owrxclient.model.WaterfallLevels;
import io.github.defective4.sdr.owrxdesktop.bandplan.Bandplan;
import io.github.defective4.sdr.owrxdesktop.cache.ReceiverCache;
import io.github.defective4.sdr.owrxdesktop.ui.BookmarksDialog.MergedLabel;
import io.github.defective4.sdr.owrxdesktop.ui.component.FFTLabel;
import io.github.defective4.sdr.owrxdesktop.ui.component.FFTPanel;
import io.github.defective4.sdr.owrxdesktop.ui.component.FFTPanel.FFTPanelListener;
import io.github.defective4.sdr.owrxdesktop.ui.component.JFrequencySpinner;
import io.github.defective4.sdr.owrxdesktop.ui.component.TuneablePanel;
import io.github.defective4.sdr.owrxdesktop.ui.component.WaterfallPanel;
import io.github.defective4.sdr.owrxdesktop.ui.event.TuningAdapter;
import io.github.defective4.sdr.owrxdesktop.ui.event.UserInteractionListener;
import io.github.defective4.sdr.owrxdesktop.ui.rendering.ReceiverModeRenderer;
import io.github.defective4.sdr.owrxdesktop.ui.settings.ReceiverUserSettings;

public class ReceiverWindow extends JFrame {

    private final JComboBox<ReceiverMode> analogBox = new JComboBox<>();
    private final Bandplan bandplan = new Bandplan();

    private int bandwidth;

    private final ReceiverCache cache;

    private int centerFrequency;
    private final JProgressBar clientsBar = new JProgressBar();

    private final JProgressBar cpuBar = new JProgressBar();

    private float cpuUsage = Integer.MIN_VALUE;

    private final JComboBox<ReceiverMode> digitalBox = new JComboBox<>();

    private boolean exiting;

    private final float fftMax = -20;
    private final float fftMin = -88;

    private final FFTPanel fftPanel;

    private final JSpinner freqSpinner = new JFrequencySpinner();
    private final JRadioButton ftlAuto = new JRadioButton("Auto");
    private final JRadioButton ftlServer = new JRadioButton("Server");

    private long lastFFTDraw;

    private final List<UserInteractionListener> listeners = new CopyOnWriteArrayList<>();
    private int maxFPS = -1;
    private float minFFT, maxFFT;
    private final JMenuItem mntmBookmarks = new JMenuItem("Bookmarks");

    private int offset;

    private final JComboBox<ReceiverProfile> profileBox = new JComboBox<>();

    private boolean profileDebounce;

    private final JButton resetScope = new JButton("Reset");

    private int scopeLower;

    private int scopeUpper;

    private WaterfallLevels serverLevels = new WaterfallLevels(-88, -20);
    private final JProgressBar signalBar = new JProgressBar();

    private int temperatureC = Integer.MIN_VALUE;

    private final ReceiverUserSettings userSettings;
    private final WaterfallPanel waterfallPanel;

    public ReceiverWindow(ReceiverUserSettings settings, ReceiverCache cache) {
        userSettings = settings;
        resetAutoFFT();
        setBounds(100, 100, 768, 550);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exit();
            }
        });
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        this.cache = cache;

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        JMenuBar menuBar = new JMenuBar();
        menuBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(menuBar);

        {
            JMenu mnFile = new JMenu("File");
            menuBar.add(mnFile);

            JMenuItem mntmQuit = new JMenuItem("Quit");
            mntmQuit.addActionListener(e -> exit());
            mntmQuit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
            mnFile.add(mntmQuit);
        }

        {
            JMenu mnWindow = new JMenu("Window");
            menuBar.add(mnWindow);

            JMenuItem mntmSettings = new JMenuItem("Settings...");
            mntmSettings.addActionListener(e -> showSettings());
            mntmSettings.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
            mnWindow.add(mntmSettings);

            mntmBookmarks.setEnabled(false);
            mntmBookmarks.addActionListener(e -> {
                ReceiverProfile profile = (ReceiverProfile) profileBox.getSelectedItem();
                MergedLabel label = BookmarksDialog.show(cache, this, profile == null ? null : profile.uuids()[1]);
                if (label != null) {
                    listeners.forEach(ls -> ls.bookmarkJumped(label));
                }
            });
            mntmBookmarks.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.CTRL_DOWN_MASK));
            mnWindow.add(mntmBookmarks);
        }

        JSplitPane splitPane = new JSplitPane();
        splitPane.setResizeWeight(1);

        mainPanel.add(splitPane);

        getContentPane().add(mainPanel);

        JPanel controlPanel = new JPanel();
        splitPane.setRightComponent(controlPanel);
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.X_AXIS));

        JTabbedPane controlTabs = new JTabbedPane(JTabbedPane.TOP);
        controlPanel.add(controlTabs);

        ReceiverModeRenderer renderer = new ReceiverModeRenderer();

        {
            JSplitPane fftPane = new JSplitPane();
            fftPane.setResizeWeight(0.75);
            fftPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
            splitPane.setLeftComponent(fftPane);

            fftPanel = new FFTPanel(bandplan);
            fftPane.setLeftComponent(fftPanel);

            waterfallPanel = new WaterfallPanel();
            waterfallPanel.setTheme(new Color[] { Color.black, Color.white });

            fftPane.setRightComponent(waterfallPanel);

            waterfallPanel.addListener(new TuningAdapter() {
                @Override
                public void scopeChanged(int scopeLower, int scopeUpper) {
                    fftPanel.setScopeUpper(scopeUpper);
                    fftPanel.setScopeLower(scopeLower);
                    listeners.forEach(ls -> ls.scopeChanged(scopeLower, scopeUpper));
                }

                @Override
                public void tuned(int offset) {
                    ReceiverWindow.this.offset = offset;
                    fftPanel.tune(offset, false, false);
                    listeners.forEach(ls -> ls.tuned(offset));
                    updateFreqSpinnerValue(offset);
                }
            });

            fftPanel.addListener(new TuningAdapter() {
                @Override
                public void scopeChanged(int scopeLower, int scopeUpper) {
                    waterfallPanel.setScopeUpper(scopeUpper);
                    waterfallPanel.setScopeLower(scopeLower);
                    listeners.forEach(ls -> ls.scopeChanged(scopeLower, scopeUpper));
                }

                @Override
                public void tuned(int offset) {
                    ReceiverWindow.this.offset = offset;
                    waterfallPanel.tune(offset, false, false);
                    listeners.forEach(ls -> ls.tuned(offset));
                    updateFreqSpinnerValue(offset);
                }

                @Override
                public void zoomChanged(int x, int width) {
                    waterfallPanel.setBounds(x, waterfallPanel.getY(), width, getHeight());
                    waterfallPanel.invalidate();
                }
            });
        }

        {
            JPanel rxCtlPanel = new JPanel();
            controlTabs.addTab("RX", null, rxCtlPanel, null);
            GridBagLayout gbl_rxCtlPanel = new GridBagLayout();
            gbl_rxCtlPanel.columnWidths = new int[] { 225, 0 };
            gbl_rxCtlPanel.rowHeights = new int[] { 0, 0, 0, 0, 0, 0 };
            gbl_rxCtlPanel.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
            gbl_rxCtlPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
            rxCtlPanel.setLayout(gbl_rxCtlPanel);

            JPanel freqPanel = new JPanel();
            compactPanel(freqPanel);
            freqPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            freqPanel
                    .setBorder(new TitledBorder(null, "Frequency", TitledBorder.LEADING, TitledBorder.TOP, null, null));
            GridBagConstraints gbc_freqPanel = new GridBagConstraints();
            gbc_freqPanel.fill = GridBagConstraints.HORIZONTAL;
            gbc_freqPanel.insets = new Insets(0, 0, 5, 0);
            gbc_freqPanel.gridx = 0;
            gbc_freqPanel.gridy = 0;
            rxCtlPanel.add(freqPanel, gbc_freqPanel);
            GridBagLayout gbl_freqPanel = new GridBagLayout();
            gbl_freqPanel.columnWidths = new int[] { 139, 4, 72, 0 };
            gbl_freqPanel.rowHeights = new int[] { 24, 0 };
            gbl_freqPanel.columnWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
            gbl_freqPanel.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
            freqPanel.setLayout(gbl_freqPanel);

            GridBagConstraints gbc_freqSpinner = new GridBagConstraints();
            gbc_freqSpinner.fill = GridBagConstraints.HORIZONTAL;
            gbc_freqSpinner.insets = new Insets(0, 0, 0, 5);
            gbc_freqSpinner.gridx = 0;
            gbc_freqSpinner.gridy = 0;
            freqPanel.add(freqSpinner, gbc_freqSpinner);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.WEST;
            gbc.insets = new Insets(0, 0, 0, 5);
            gbc.gridx = 1;
            gbc.gridy = 0;
            JLabel label = new JLabel(" ");
            freqPanel.add(label, gbc);

            JButton goFreqBtn = new JButton("Go");
            GridBagConstraints gbc_goFreqBtn = new GridBagConstraints();
            gbc_goFreqBtn.anchor = GridBagConstraints.WEST;
            gbc_goFreqBtn.gridx = 2;
            gbc_goFreqBtn.gridy = 0;
            freqPanel.add(goFreqBtn, gbc_goFreqBtn);

            ((NumberEditor) freqSpinner.getEditor()).getTextField().addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) goFreqBtn.doClick();
                }
            });

            goFreqBtn.addActionListener(e -> {
                int freq = (int) freqSpinner.getValue();
                int offset = freq - centerFrequency;
                if (Math.abs(offset) > bandwidth / 2) {
                    if (settings.isEnableFreeTuning()) {
                        listeners.forEach(ls -> ls.freeTune(freq));
                    } else {
                        if (JOptionPane.showOptionDialog(this,
                                new String[] {
                                        "You are trying to tune outside of the current profile's frequency range.",
                                        "This requires free tuning mode to be enabled in Settings > Receiver" },
                                "Can't tune", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null,
                                new String[] { "Take me there", "Cancel" }, null) == 0) {
                            showSettings();
                        }
                        updateFreqSpinnerValue(this.offset);
                    }
                    return;
                }
                tune(offset, true, false);
            });

            JPanel profilePanel = new JPanel();
            compactPanel(profilePanel);
            profilePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            profilePanel
                    .setBorder(new TitledBorder(null, "Profile", TitledBorder.LEADING, TitledBorder.TOP, null, null));
            GridBagConstraints gbc_profilePanel = new GridBagConstraints();
            gbc_profilePanel.fill = GridBagConstraints.HORIZONTAL;
            gbc_profilePanel.insets = new Insets(0, 0, 5, 0);
            gbc_profilePanel.gridx = 0;
            gbc_profilePanel.gridy = 1;
            rxCtlPanel.add(profilePanel, gbc_profilePanel);
            GridBagLayout gbl_profilePanel = new GridBagLayout();
            gbl_profilePanel.columnWidths = new int[] { 220, 0 };
            gbl_profilePanel.rowHeights = new int[] { 24, 0 };
            gbl_profilePanel.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
            gbl_profilePanel.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
            profilePanel.setLayout(gbl_profilePanel);

            profileBox.setAlignmentX(Component.LEFT_ALIGNMENT);
            GridBagConstraints gbc_profileBox = new GridBagConstraints();
            gbc_profileBox.fill = GridBagConstraints.HORIZONTAL;
            gbc_profileBox.gridx = 0;
            gbc_profileBox.gridy = 0;
            profilePanel.add(profileBox, gbc_profileBox);

            profileBox.addActionListener(e -> {
                if (profileDebounce) return;
                disableControls();
                ReceiverProfile profile = (ReceiverProfile) profileBox.getSelectedItem();
                if (profile != null) listeners.forEach(ls -> ls.profileChanged(profile));
            });

            JPanel modePanel = new JPanel();
            modePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            compactPanel(modePanel);
            modePanel.setBorder(new TitledBorder(null, "Mode", TitledBorder.LEADING, TitledBorder.TOP, null, null));
            GridBagConstraints gbc_modePanel = new GridBagConstraints();
            gbc_modePanel.fill = GridBagConstraints.HORIZONTAL;
            gbc_modePanel.insets = new Insets(0, 0, 5, 0);
            gbc_modePanel.gridx = 0;
            gbc_modePanel.gridy = 2;
            rxCtlPanel.add(modePanel, gbc_modePanel);
            GridBagLayout gbl_modePanel = new GridBagLayout();
            gbl_modePanel.columnWidths = new int[] { 220, 0 };
            gbl_modePanel.rowHeights = new int[] { 18, 24, 18, 24, 0 };
            gbl_modePanel.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
            gbl_modePanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
            modePanel.setLayout(gbl_modePanel);

            GridBagConstraints gbc_1 = new GridBagConstraints();
            gbc_1.anchor = GridBagConstraints.WEST;
            gbc_1.insets = new Insets(0, 0, 5, 0);
            gbc_1.gridx = 0;
            gbc_1.gridy = 0;
            JLabel label_1 = new JLabel("Primary");
            modePanel.add(label_1, gbc_1);
            analogBox.setRenderer(renderer);

            analogBox.setAlignmentX(Component.LEFT_ALIGNMENT);
            GridBagConstraints gbc_analogBox = new GridBagConstraints();
            gbc_analogBox.fill = GridBagConstraints.HORIZONTAL;
            gbc_analogBox.insets = new Insets(0, 0, 5, 0);
            gbc_analogBox.gridx = 0;
            gbc_analogBox.gridy = 1;
            modePanel.add(analogBox, gbc_analogBox);

            analogBox.addActionListener(e -> {
                ReceiverMode selected = (ReceiverMode) analogBox.getSelectedItem();
                if (selected != null) {
                    if (digitalBox.getItemCount() > 0) {
                        ReceiverMode digi = (ReceiverMode) digitalBox.getSelectedItem();
                        if (digi != null) {
                            boolean has = false;
                            for (String mod : digi.underlying()) {
                                if (mod.equals(selected.modulation())) {
                                    has = true;
                                    break;
                                }
                            }
                            if (!has) {
                                digitalBox.setSelectedIndex(0);
                            }
                        }
                    }
                }
                updateMode();
            });

            JLabel lblDigital = new JLabel("Digital");
            GridBagConstraints gbc_lblDigital = new GridBagConstraints();
            gbc_lblDigital.anchor = GridBagConstraints.WEST;
            gbc_lblDigital.insets = new Insets(0, 0, 5, 0);
            gbc_lblDigital.gridx = 0;
            gbc_lblDigital.gridy = 2;
            modePanel.add(lblDigital, gbc_lblDigital);
            digitalBox.setRenderer(renderer);

            digitalBox.setAlignmentX(Component.LEFT_ALIGNMENT);
            GridBagConstraints gbc_digitalBox = new GridBagConstraints();
            gbc_digitalBox.fill = GridBagConstraints.HORIZONTAL;
            gbc_digitalBox.gridx = 0;
            gbc_digitalBox.gridy = 3;
            modePanel.add(digitalBox, gbc_digitalBox);

            digitalBox.addActionListener(e -> {
                ReceiverMode selected = (ReceiverMode) digitalBox.getSelectedItem();
                if (selected != null) {
                    int count = analogBox.getItemCount();
                    for (int i = 0; i < count; i++) {
                        ReceiverMode analogItem = analogBox.getItemAt(i);
                        if (analogItem != null && analogItem.modulation().equals(selected.underlying()[0])) {
                            analogBox.setSelectedIndex(i);
                            break;
                        }
                    }
                }
                updateMode();
            });

            JPanel scopePanel = new JPanel();
            compactPanel(scopePanel);
            scopePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            scopePanel.setBorder(new TitledBorder(null, "Scope", TitledBorder.LEADING, TitledBorder.TOP, null, null));
            GridBagConstraints gbc_scopePanel = new GridBagConstraints();
            gbc_scopePanel.fill = GridBagConstraints.HORIZONTAL;
            gbc_scopePanel.insets = new Insets(0, 0, 5, 0);
            gbc_scopePanel.gridx = 0;
            gbc_scopePanel.gridy = 3;
            rxCtlPanel.add(scopePanel, gbc_scopePanel);
            FlowLayout fl_scopePanel = new FlowLayout(FlowLayout.LEFT, 0, 0);
            scopePanel.setLayout(fl_scopePanel);

            resetScope.setEnabled(false);
            resetScope.addActionListener(e -> {
                setScopeLower(scopeLower);
                setScopeUpper(scopeUpper);
                listeners.forEach(ls -> ls.scopeChanged(scopeLower, scopeUpper));
            });
            scopePanel.add(resetScope);

            JCheckBox symmetricalCheck = new JCheckBox("Symmetrical");
            symmetricalCheck.setSelected(true);
            symmetricalCheck.addActionListener(e -> {
                for (TuneablePanel panel : getPanels()) {
                    panel.setSymmetricalScope(symmetricalCheck.isSelected());
                }
            });
            scopePanel.add(symmetricalCheck);

            JPanel levelsPanel = new JPanel();
            compactPanel(levelsPanel);
            levelsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            levelsPanel.setBorder(new TitledBorder(null, "Levels", TitledBorder.LEADING, TitledBorder.TOP, null, null));
            GridBagConstraints gbc_levelsPanel = new GridBagConstraints();
            gbc_levelsPanel.fill = GridBagConstraints.HORIZONTAL;
            gbc_levelsPanel.gridx = 0;
            gbc_levelsPanel.gridy = 4;
            rxCtlPanel.add(levelsPanel, gbc_levelsPanel);
            levelsPanel.setLayout(new BoxLayout(levelsPanel, BoxLayout.Y_AXIS));

            levelsPanel.add(new JLabel("Signal"));
            signalBar.setString(" ");
            levelsPanel.add(signalBar);
            signalBar.setStringPainted(true);
            signalBar.setAlignmentX(Component.LEFT_ALIGNMENT);

            levelsPanel.add(new JLabel("Clients"));
            clientsBar.setString(" ");
            levelsPanel.add(clientsBar);
            clientsBar.setStringPainted(true);

            clientsBar.setAlignmentX(Component.LEFT_ALIGNMENT);

            levelsPanel.add(new JLabel("CPU"));
            cpuBar.setString(" ");
            levelsPanel.add(cpuBar);
            cpuBar.setStringPainted(true);

            cpuBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        }

        {
            JPanel fftCtlPanel = new JPanel();
            controlTabs.addTab("FFT", null, fftCtlPanel, null);
            GridBagLayout gbl_fftCtlPanel = new GridBagLayout();
            gbl_fftCtlPanel.columnWidths = new int[] { 230, 0 };
            gbl_fftCtlPanel.rowHeights = new int[] { 0, 0, 0, 0 };
            gbl_fftCtlPanel.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
            gbl_fftCtlPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
            fftCtlPanel.setLayout(gbl_fftCtlPanel);

            JPanel featPanel = new JPanel();
            featPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            compactPanel(featPanel);
            featPanel.setBorder(new TitledBorder(null, "Features", TitledBorder.LEADING, TitledBorder.TOP, null, null));
            GridBagConstraints gbc_featPanel = new GridBagConstraints();
            gbc_featPanel.fill = GridBagConstraints.HORIZONTAL;
            gbc_featPanel.insets = new Insets(0, 0, 5, 0);
            gbc_featPanel.gridx = 0;
            gbc_featPanel.gridy = 0;
            fftCtlPanel.add(featPanel, gbc_featPanel);
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

            JButton btnResetMax = new JButton("Reset max");
            featPanel.add(btnResetMax);

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

            confirmComponentState(maxFpsSlider);

            maxDrawCheck.addActionListener(e -> {
                fftPanel.setDrawMaxValues(maxDrawCheck.isSelected());
                btnResetMax.setEnabled(maxDrawCheck.isSelected());
                fftPanel.repaint();
            });

            btnResetMax.addActionListener(e -> fftPanel.resetMaxFFT());

            confirmComponentState(maxDrawCheck);

            JPanel levelsPanel = new JPanel();
            compactPanel(levelsPanel);
            levelsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            levelsPanel.setBorder(new TitledBorder(null, "Levels", TitledBorder.LEADING, TitledBorder.TOP, null, null));
            GridBagConstraints gbc_levelsPanel = new GridBagConstraints();
            gbc_levelsPanel.fill = GridBagConstraints.HORIZONTAL;
            gbc_levelsPanel.insets = new Insets(0, 0, 5, 0);
            gbc_levelsPanel.gridx = 0;
            gbc_levelsPanel.gridy = 1;
            fftCtlPanel.add(levelsPanel, gbc_levelsPanel);
            levelsPanel.setLayout(new BoxLayout(levelsPanel, BoxLayout.Y_AXIS));

            JPanel fftLevelModePanel = new JPanel();
            FlowLayout flowLayout = (FlowLayout) fftLevelModePanel.getLayout();
            flowLayout.setAlignment(FlowLayout.LEFT);
            fftLevelModePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            levelsPanel.add(fftLevelModePanel);

            ftlServer.setSelected(true);
            ftlServer.setToolTipText("Use waterfall levels provided by the server");
            fftLevelModePanel.add(ftlServer);

            ftlAuto.setToolTipText("Automatically adjust waterfall levels based on signal");
            fftLevelModePanel.add(ftlAuto);

            JRadioButton ftlManual = new JRadioButton("Manual");
            ftlManual.setToolTipText("Adjust waterfall levels manually");
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
            confirmComponentState(ftlManual);

            confirmComponentState(minSlider);
            confirmComponentState(maxSlider);

            JPanel labelsPanel = new JPanel();
            compactPanel(labelsPanel);
            labelsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            labelsPanel.setBorder(new TitledBorder(null, "Labels", TitledBorder.LEADING, TitledBorder.TOP, null, null));
            GridBagConstraints gbc_labelsPanel = new GridBagConstraints();
            gbc_labelsPanel.fill = GridBagConstraints.HORIZONTAL;
            gbc_labelsPanel.gridx = 0;
            gbc_labelsPanel.gridy = 2;
            fftCtlPanel.add(labelsPanel, gbc_labelsPanel);
            labelsPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));

            JCheckBox dialCheck = new JCheckBox("Dial frequencies");
            dialCheck.addActionListener(e -> fftPanel.setLabelRender(FFTLabel.Type.DIAL, dialCheck.isSelected()));
            dialCheck.setSelected(true);
            labelsPanel.add(dialCheck);

            confirmComponentState(dialCheck);

            JCheckBox bookmarksCheck = new JCheckBox("Bookmarks");
            bookmarksCheck.addActionListener(
                    e -> fftPanel.setLabelRender(FFTLabel.Type.SRV_BOOKMARK, bookmarksCheck.isSelected()));
            bookmarksCheck.setSelected(true);
            labelsPanel.add(bookmarksCheck);
        }

        {

            JPanel audioCtlPanel = new JPanel();
            controlTabs.addTab("Audio", null, audioCtlPanel, null);
            GridBagLayout gbl_audioCtlPanel = new GridBagLayout();
            gbl_audioCtlPanel.columnWidths = new int[] { 0, 0 };
            gbl_audioCtlPanel.rowHeights = new int[] { 0, 0 };
            gbl_audioCtlPanel.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
            gbl_audioCtlPanel.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
            audioCtlPanel.setLayout(gbl_audioCtlPanel);

            JPanel audioPanel = new JPanel();
            GridBagConstraints gbc_audioPanel = new GridBagConstraints();
            gbc_audioPanel.fill = GridBagConstraints.HORIZONTAL;
            gbc_audioPanel.gridx = 0;
            gbc_audioPanel.gridy = 0;
            audioCtlPanel.add(audioPanel, gbc_audioPanel);
            audioPanel.setBorder(new TitledBorder(null, "Audio", TitledBorder.LEADING, TitledBorder.TOP, null, null));
            audioPanel.setLayout(new BoxLayout(audioPanel, BoxLayout.Y_AXIS));

            audioPanel.add(new JLabel("Volume"));

            JSlider volumeSlider = new JSlider();
            volumeSlider.setLabelTable(volumeSlider.createStandardLabels(100));
            volumeSlider.setPaintLabels(true);
            volumeSlider.setAlignmentX(Component.LEFT_ALIGNMENT);
            volumeSlider.setMinimum(0);
            volumeSlider.setMaximum(100);
            volumeSlider.setValue(100);
            audioPanel.add(volumeSlider);

            JCheckBox muteCheck = new JCheckBox("Mute");
            audioPanel.add(muteCheck);
            muteCheck.addActionListener(e -> {
                boolean muted = muteCheck.isSelected();
                volumeSlider.setEnabled(!muted);
                listeners.forEach(ls -> ls.muteToggled(muted));
            });

            volumeSlider
                    .addChangeListener(e -> listeners.forEach(ls -> ls.volumeChanged(volumeSlider.getValue() / 100f)));

            confirmComponentState(muteCheck);

            disableControls();
        }
    }

    public boolean addFFTPanelListener(FFTPanelListener listener) {
        return fftPanel.addPanelListener(listener);
    }

    public boolean addListener(UserInteractionListener listener) {
        return listeners.add(Objects.requireNonNull(listener));
    }

    public void disableControls() {
        setControls(false);
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

    public void enableControls() {
        setControls(true);
    }

    public List<ReceiverMode> getAnalogModes() {
        List<ReceiverMode> mode = new ArrayList<>();
        int count = analogBox.getItemCount();
        for (int i = 0; i < count; i++) {
            mode.add(analogBox.getItemAt(i));
        }
        return Collections.unmodifiableList(mode);
    }

    public Bandplan getBandplan() {
        return bandplan;
    }

    public int getBandwidth() {
        return bandwidth;
    }

    public int getCenterFrequency() {
        return centerFrequency;
    }

    public List<ReceiverMode> getDigitalModes() {
        List<ReceiverMode> mode = new ArrayList<>();
        int count = digitalBox.getItemCount();
        for (int i = 0; i < count; i++) {
            mode.add(digitalBox.getItemAt(i));
        }
        return Collections.unmodifiableList(mode);
    }

    public FFTPanel getFftPanel() {
        return fftPanel;
    }

    public List<UserInteractionListener> getListeners() {
        return Collections.unmodifiableList(listeners);
    }

    public TuneablePanel[] getPanels() {
        return new TuneablePanel[] { waterfallPanel, fftPanel };
    }

    public ReceiverMode getPrimaryMode() {
        return (ReceiverMode) analogBox.getSelectedItem();
    }

    public Optional<ReceiverProfile> getProfileById(String id) {
        int count = profileBox.getItemCount();
        for (int i = 0; i < count; i++) {
            ReceiverProfile item = profileBox.getItemAt(i);
            if (item.uuids()[1].toString().equals(id)) {
                return Optional.of(item);
            }
        }
        return Optional.empty();
    }

    public Optional<ReceiverMode> getSecondaryMode() {
        return Optional.ofNullable((ReceiverMode) digitalBox.getSelectedItem());
    }

    public WaterfallLevels getServerLevels() {
        return serverLevels;
    }

    public boolean removeListener(UserInteractionListener listener) {
        return listeners.remove(listener);
    }

    public void resetAutoFFT() {
        minFFT = Integer.MAX_VALUE;
        maxFFT = Integer.MIN_VALUE;
    }

    public void resetLabels() {
        fftPanel.resetLabels();
    }

    public void setBandwidth(int bandwidth) {
        this.bandwidth = bandwidth;
        for (TuneablePanel fftPanel : getPanels()) fftPanel.setBandwidth(bandwidth);
    }

    public void setCenterFrequency(int centerFrequency) {
        this.centerFrequency = centerFrequency;
        for (TuneablePanel fftPanel : getPanels()) fftPanel.setCenterFrequency(centerFrequency);
        updateFreqSpinnerValue(0);
    }

    public void setClients(int clients) {
        clientsBar.setValue(clients);
        clientsBar.setString(clientsBar.getValue() + "/" + clientsBar.getMaximum());
    }

    public void setColorMixing(boolean colorMixing) {
        waterfallPanel.setColorMixing(colorMixing);
    }

    public void setCPUUsage(float cpuUsage) {
        this.cpuUsage = cpuUsage;
        updateCPU();
    }

    public void setFFTMax(float f) {
        for (TuneablePanel fftPanel : getPanels()) fftPanel.setFFTMax(f);
    }



    public void setFFTMin(float f) {
        for (TuneablePanel fftPanel : getPanels()) fftPanel.setFFTMin(f);
    }

    public void setLabels(Collection<FFTLabel> labels) {
        fftPanel.setLabels(labels);
    }

    public void setMaxClients(int maxClients) {
        clientsBar.setMaximum(maxClients);
        clientsBar.setString(clientsBar.getValue() + "/" + clientsBar.getMaximum());
    }

    public void setScopeLower(int scopeLower) {
        this.scopeLower = scopeLower;
        resetScope.setEnabled(true);
        for (TuneablePanel fftPanel : getPanels()) fftPanel.setScopeLower(scopeLower);
    }

    public void setScopeUpper(int scopeUpper) {
        this.scopeUpper = scopeUpper;
        resetScope.setEnabled(true);
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

    public void setStartingMode(ReceiverMode mode) {
        int count = digitalBox.getItemCount();
        boolean found = false;
        for (int i = 0; i < count; i++) {
            ReceiverMode m = digitalBox.getItemAt(i);
            if (m != null && m.name().equals(mode.name())) {
                found = true;
                digitalBox.setSelectedIndex(i);
                break;
            }
        }
        if (!found) {
            if (digitalBox.getItemCount() > 0) digitalBox.setSelectedIndex(0);
            count = analogBox.getItemCount();
            for (int i = 0; i < count; i++) {
                ReceiverMode m = analogBox.getItemAt(i);
                if (m != null && m.name().equals(mode.name())) {
                    analogBox.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    public void setTemperature(int temperatureC) {
        this.temperatureC = temperatureC;
        updateCPU();
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

    public void showSettings() {
        SettingsDialog.show(this, userSettings);
        listeners.forEach(ls -> ls.settingsChanged());
    }

    public void tune(int offset) {
        this.offset = offset;
        for (TuneablePanel fftPanel : getPanels()) fftPanel.tune(offset);
        updateFreqSpinnerValue(offset);
    }

    public void tune(int offset, boolean fireEvents, boolean snap) {
        this.offset = offset;
        for (TuneablePanel fftPanel : getPanels()) fftPanel.tune(offset, fireEvents, snap);
        updateFreqSpinnerValue(offset);
    }

    public void updateBandplan() {
        fftPanel.updateVisibleBands();
    }

    public void updateModes(ReceiverMode[] modes) {
        analogBox.removeAllItems();
        digitalBox.removeAllItems();

        digitalBox.addItem(null);
        analogBox.addItem(ReceiverMode.EMPTY);

        for (ReceiverMode mode : modes) {
            if (mode.underlying() != null && mode.underlying().length > 0) {
                digitalBox.addItem(mode);
            } else {
                analogBox.addItem(mode);
            }
        }
    }

    public void updateProfile(ReceiverProfile profile) {
        mntmBookmarks.setEnabled(true);
        profileDebounce = true;
        profileBox.setSelectedItem(profile);
        profileDebounce = false;
    }

    public void updateProfiles(ReceiverProfile[] profiles) {
        profileDebounce = true;
        profileBox.removeAllItems();
        for (ReceiverProfile profile : profiles) profileBox.addItem(profile);
        invalidate();
        profileDebounce = false;
    }

    public void updateSignal(double val) {
        double log = 10 * Math.log10(val);
        double percent = (log - (fftMin - 20)) / (fftMax + 20 - (fftMin - 20));

        double db = (int) (log * 10d) / 10d;
        signalBar.setValue((int) (percent * 100));
        signalBar.setString(String.format("%s dB", db));
    }

    private void exit() {
        if (exiting) return;
        if (JOptionPane.showOptionDialog(this, "Are you sure you want to close the receiver?", "Exiting",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null) == JOptionPane.YES_OPTION) {
            exiting = true;
            System.exit(0);
        }
    }

    private void setControls(boolean state) {
        profileBox.setEnabled(state);
        analogBox.setEnabled(state);
        digitalBox.setEnabled(state);
        freqSpinner.setEnabled(state);
    }

    private void updateCPU() {
        String str;
        int cpuUsage = (int) Math.ceil(this.cpuUsage * 100d);
        if (temperatureC > Integer.MIN_VALUE)
            str = String.format("%s / %s°C", cpuUsage + "%", temperatureC);
        else
            str = String.format("%s", cpuUsage + "%");
        cpuBar.setString(str);
        cpuBar.setValue(cpuUsage);
    }

    private void updateFreqSpinnerValue(int offset) {
        freqSpinner.setValue(centerFrequency + offset);
    }

    private void updateMode() {
        ReceiverMode primary = (ReceiverMode) analogBox.getSelectedItem();
        ReceiverMode secondary = (ReceiverMode) digitalBox.getSelectedItem();
        listeners.forEach(ls -> ls.modeChanged(primary, secondary));
        Bandpass bandpass = (secondary == null || secondary.bandpass() == null ? primary : secondary).bandpass();
        if (bandpass != null) {
            setScopeLower(bandpass.lowCut());
            setScopeUpper(bandpass.highCut());
        }
    }

    private static void compactPanel(JComponent featPanel) {
        featPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 0));
    }

    private static void confirmComponentState(AbstractButton component) {
        for (ActionListener ls : component.getActionListeners()) ls.actionPerformed(null);
    }

    private static void confirmComponentState(JSlider component) {
        for (ChangeListener ls : component.getChangeListeners()) ls.stateChanged(null);
    }
}
