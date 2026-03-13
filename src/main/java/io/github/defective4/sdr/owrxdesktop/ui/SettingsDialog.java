package io.github.defective4.sdr.owrxdesktop.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import io.github.defective4.sdr.owrxdesktop.ui.settings.ReceiverUserSettings;
import io.github.defective4.sdr.owrxdesktop.ui.settings.waterfall.BuiltinWaterfallTheme;
import io.github.defective4.sdr.owrxdesktop.ui.settings.waterfall.WaterfallThemeMode;

public class SettingsDialog extends JDialog {

    private final JCheckBox freeTuningCheck = new JCheckBox("Enable free tuning");
    private final JPasswordField magicKeyField = new JPasswordField();
    private final JRadioButton rdbtnBuiltin = new JRadioButton("Built-in: ");
    private final JRadioButton rdbtnCustom = new JRadioButton("Custom (One hex color per line, each starting with #):");
    private final JRadioButton rdbtnServerprovidedConfiguration = new JRadioButton("Server-provided configuration");
    private final JTextArea themeArea = new JTextArea();
    private final JComboBox<BuiltinWaterfallTheme> themesBox = new JComboBox<>();

    private SettingsDialog(Window parent, ReceiverUserSettings settings) {
        super(parent);
        setTitle("Receiver settings");
        setModal(true);
        setSize(450, 500);
        setMinimumSize(new Dimension(450, 500));
        setLocationRelativeTo(parent);
        getContentPane().setLayout(new BorderLayout());
        JPanel contentPanel = new JPanel();
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.X_AXIS));

        {
            JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
            contentPanel.add(tabbedPane);
            {
                JTabbedPane rxTabs = new JTabbedPane(JTabbedPane.TOP);
                tabbedPane.addTab("Receiver", null, rxTabs, null);
                {
                    JPanel mainPanel = new JPanel();
                    rxTabs.addTab("Main", null, mainPanel, null);
                    GridBagLayout gbl_mainPanel = new GridBagLayout();
                    gbl_mainPanel.columnWidths = new int[] { 0, 0 };
                    gbl_mainPanel.rowHeights = new int[] { 0, 0 };
                    gbl_mainPanel.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
                    gbl_mainPanel.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
                    mainPanel.setLayout(gbl_mainPanel);
                    {
                        JPanel panel = new JPanel();
                        panel.setBorder(new TitledBorder(null, "Free tuning", TitledBorder.LEADING, TitledBorder.TOP,
                                null, null));
                        GridBagConstraints gbc_panel = new GridBagConstraints();
                        gbc_panel.anchor = GridBagConstraints.NORTH;
                        gbc_panel.fill = GridBagConstraints.HORIZONTAL;
                        gbc_panel.gridx = 0;
                        gbc_panel.gridy = 0;
                        mainPanel.add(panel, gbc_panel);
                        GridBagLayout gbl_panel = new GridBagLayout();
                        gbl_panel.columnWidths = new int[] { 0, 0 };
                        gbl_panel.rowHeights = new int[] { 0, 0, 0, 0 };
                        gbl_panel.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
                        gbl_panel.rowWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
                        panel.setLayout(gbl_panel);
                        {
                            GridBagConstraints gbc_chckbxEnableFreeTuning = new GridBagConstraints();
                            gbc_chckbxEnableFreeTuning.anchor = GridBagConstraints.WEST;
                            gbc_chckbxEnableFreeTuning.insets = new Insets(0, 0, 5, 0);
                            gbc_chckbxEnableFreeTuning.gridx = 0;
                            gbc_chckbxEnableFreeTuning.gridy = 0;
                            panel.add(freeTuningCheck, gbc_chckbxEnableFreeTuning);
                        }
                        {
                            JLabel lblMagicKeyoptional = new JLabel("Magic key (Optional):");
                            GridBagConstraints gbc_lblMagicKeyoptional = new GridBagConstraints();
                            gbc_lblMagicKeyoptional.anchor = GridBagConstraints.WEST;
                            gbc_lblMagicKeyoptional.insets = new Insets(0, 0, 5, 0);
                            gbc_lblMagicKeyoptional.gridx = 0;
                            gbc_lblMagicKeyoptional.gridy = 1;
                            panel.add(lblMagicKeyoptional, gbc_lblMagicKeyoptional);
                        }
                        {
                            magicKeyField.setColumns(16);
                            GridBagConstraints gbc_passwordField = new GridBagConstraints();
                            gbc_passwordField.anchor = GridBagConstraints.WEST;
                            gbc_passwordField.gridx = 0;
                            gbc_passwordField.gridy = 2;
                            panel.add(magicKeyField, gbc_passwordField);
                        }

                        freeTuningCheck.setSelected(settings.isEnableFreeTuning());
                        magicKeyField.setText(settings.getMagicKey());

                        ActionListener a = e -> magicKeyField.setEnabled(freeTuningCheck.isSelected());

                        freeTuningCheck.addActionListener(a);

                        a.actionPerformed(null);
                    }
                }
            }
            {
                JTabbedPane fftTabs = new JTabbedPane(JTabbedPane.TOP);
                tabbedPane.addTab("FFT", null, fftTabs, null);
                {
                    JScrollPane scrollPane = new JScrollPane();
                    fftTabs.addTab("Waterfall theme", null, scrollPane, null);
                    {
                        JPanel fftThemePanel = new JPanel();
                        fftThemePanel.setBorder(new EmptyBorder(0, 8, 8, 8));
                        scrollPane.setViewportView(fftThemePanel);
                        fftThemePanel.setLayout(new BoxLayout(fftThemePanel, BoxLayout.Y_AXIS));
                        JPanel builtin = new JPanel();
                        builtin.setAlignmentX(Component.LEFT_ALIGNMENT);
                        fftThemePanel.add(rdbtnServerprovidedConfiguration);
                        fftThemePanel.add(builtin);
                        builtin.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 5));
                        builtin.add(rdbtnBuiltin);
                        for (BuiltinWaterfallTheme theme : BuiltinWaterfallTheme.values()) themesBox.addItem(theme);
                        builtin.add(themesBox);
                        fftThemePanel.add(rdbtnCustom);
                        fftThemePanel.add(new JSeparator());
                        themeArea.setAlignmentX(Component.LEFT_ALIGNMENT);
                        fftThemePanel.add(themeArea);

                        ButtonGroup themeGroup = new ButtonGroup();
                        themeGroup.add(rdbtnServerprovidedConfiguration);
                        themeGroup.add(rdbtnBuiltin);
                        themeGroup.add(rdbtnCustom);

                        ActionListener ls = e -> {
                            themeArea.setEnabled(rdbtnCustom.isSelected());
                            themesBox.setEnabled(rdbtnBuiltin.isSelected());
                        };

                        (switch (settings.getWaterfallThemeMode()) {
                            default -> rdbtnServerprovidedConfiguration;
                            case CUSTOM -> rdbtnCustom;
                            case BUILTIN -> rdbtnBuiltin;
                        }).setSelected(true);

                        themesBox.setSelectedItem(settings.getSelectedBuiltinWaterfallTheme());
                        themeArea.setText(String.join("\n", settings.getWaterfallCustomTheme().toArray(new String[0])));

                        rdbtnServerprovidedConfiguration.addActionListener(ls);
                        rdbtnBuiltin.addActionListener(ls);
                        rdbtnCustom.addActionListener(ls);

                        ls.actionPerformed(null);
                    }
                }
            }
        }
        {
            JPanel buttonPane = new JPanel();
            buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
            getContentPane().add(buttonPane, BorderLayout.SOUTH);
            {
                JButton okButton = new JButton("Save");
                okButton.setActionCommand("OK");
                okButton.addActionListener(e -> {
                    WaterfallThemeMode mode = WaterfallThemeMode.SERVER;
                    if (rdbtnBuiltin.isSelected()) mode = WaterfallThemeMode.BUILTIN;
                    if (rdbtnCustom.isSelected()) mode = WaterfallThemeMode.CUSTOM;

                    if (!validateSettings()) return;

                    settings.setSelectedBuiltinWaterfallTheme((BuiltinWaterfallTheme) themesBox.getSelectedItem());
                    settings.setWaterfallCustomTheme(List.of(themeArea.getText().split("\n")));
                    settings.setWaterfallThemeMode(mode);
                    settings.setMagicKey(new String(magicKeyField.getPassword()));
                    settings.setEnableFreeTuning(freeTuningCheck.isSelected());
                    dispose();
                });
                buttonPane.add(okButton);
                getRootPane().setDefaultButton(okButton);
            }
            {
                JButton cancelButton = new JButton("Cancel");
                cancelButton.setActionCommand("Cancel");
                cancelButton.addActionListener(e -> dispose());
                buttonPane.add(cancelButton);
            }
        }
    }

    private boolean validateSettings() {
        if (rdbtnCustom.isSelected()) {
            for (String line : themeArea.getText().split("\n")) {
                try {
                    Color.decode(line);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Some color lines in the custom theme are invalid.",
                            "Invalid theem", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
        }
        return true;
    }

    public static void show(Window parent, ReceiverUserSettings initialSettings) {
        SettingsDialog dialog = new SettingsDialog(parent, initialSettings);
        dialog.setVisible(true);
    }
}
