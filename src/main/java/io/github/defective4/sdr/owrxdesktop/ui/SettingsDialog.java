package io.github.defective4.sdr.owrxdesktop.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import io.github.defective4.sdr.owrxdesktop.ui.settings.ReceiverUserSettings;
import io.github.defective4.sdr.owrxdesktop.ui.settings.waterfall.BuiltinWaterfallTheme;
import io.github.defective4.sdr.owrxdesktop.ui.settings.waterfall.WaterfallThemeMode;

public class SettingsDialog extends JDialog {

    private ReceiverUserSettings newSettings;

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

                    newSettings = new ReceiverUserSettings((BuiltinWaterfallTheme) themesBox.getSelectedItem(),
                            List.of(themeArea.getText().split("\n")), mode);
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

    public static ReceiverUserSettings show(Window parent, ReceiverUserSettings initialSettings) {
        SettingsDialog dialog = new SettingsDialog(parent, initialSettings);
        dialog.setVisible(true);
        return dialog.newSettings;
    }
}
