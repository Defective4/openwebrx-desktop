package io.github.defective4.sdr.owrxdesktop.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

public class SettingsDialog extends JDialog {

    private SettingsDialog(Window parent) {
        super(parent);
        setTitle("Receiver settings");
        setModal(true);
        setSize(450, 300);
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
                        JRadioButton rdbtnServerprovidedConfiguration = new JRadioButton(
                                "Server-provided configuration");
                        fftThemePanel.add(rdbtnServerprovidedConfiguration);
                        JPanel builtin = new JPanel();
                        builtin.setAlignmentX(Component.LEFT_ALIGNMENT);
                        fftThemePanel.add(builtin);
                        builtin.setLayout(new GridLayout(0, 3, 0, 0));
                        JRadioButton rdbtnBuiltin = new JRadioButton("Built-in: ");
                        builtin.add(rdbtnBuiltin);
                        JComboBox themesBox = new JComboBox();
                        builtin.add(themesBox);
                        JPanel custom = new JPanel();
                        custom.setAlignmentX(Component.LEFT_ALIGNMENT);
                        fftThemePanel.add(custom);
                        custom.setLayout(new BoxLayout(custom, BoxLayout.X_AXIS));
                        JRadioButton rdbtnCustom = new JRadioButton("Custom");
                        custom.add(rdbtnCustom);
                        custom.add(new JLabel("(One hex color per line, each starting with #):"));
                        JTextArea themeArea = new JTextArea();
                        themeArea.setText("#000000\n#ffffff");
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
                okButton.addActionListener(e -> dispose());
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

    public static void show(Window parent) {
        SettingsDialog dialog = new SettingsDialog(parent);
        dialog.setVisible(true);
    }
}
