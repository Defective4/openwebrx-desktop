package io.github.defective4.sdr.owrxdesktop.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import io.github.defective4.sdr.owrxdesktop.application.ApplicationSettings;

public class ApplicationSettingsDialog extends JDialog {

    private final JCheckBox autoDownloadCheck = new JCheckBox("Auto-download public receiver listings");
    private final JCheckBox autoRefreshCheck = new JCheckBox("Auto-refresh receivers on startup");
    private final JSpinner networkWorkers = new JSpinner();

    public ApplicationSettingsDialog(Frame parent, ApplicationSettings settings) {
        super(parent);
        setTitle("Application settings");
        setModal(true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());
        JPanel contentPanel = new JPanel();
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        contentPanel.setLayout(new BorderLayout(0, 0));
        {
            JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
            contentPanel.add(tabbedPane, BorderLayout.CENTER);
            {
                JPanel panel = new JPanel();
                panel.setBorder(new EmptyBorder(0, 5, 0, 5));
                tabbedPane.addTab("Network", null, panel, null);
                GridBagLayout gbl_panel = new GridBagLayout();
                gbl_panel.columnWidths = new int[] { 0, 0 };
                gbl_panel.rowHeights = new int[] { 0, 0, 0, 0 };
                gbl_panel.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
                gbl_panel.rowWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
                panel.setLayout(gbl_panel);
                {
                    JPanel networkWorkersPanel = new JPanel();
                    networkWorkersPanel.setBorder(new TitledBorder(null, "Max network workers", TitledBorder.LEADING,
                            TitledBorder.TOP, null, null));
                    GridBagConstraints gbc_panel_1 = new GridBagConstraints();
                    gbc_panel_1.fill = GridBagConstraints.HORIZONTAL;
                    gbc_panel_1.insets = new Insets(0, 0, 5, 0);
                    gbc_panel_1.anchor = GridBagConstraints.NORTH;
                    gbc_panel_1.gridx = 0;
                    gbc_panel_1.gridy = 0;
                    panel.add(networkWorkersPanel, gbc_panel_1);
                    networkWorkersPanel.setLayout(new BoxLayout(networkWorkersPanel, BoxLayout.Y_AXIS));
                    networkWorkers.setAlignmentX(Component.LEFT_ALIGNMENT);
                    networkWorkers.setModel(new SpinnerNumberModel(1, 1, 100, 1));
                    networkWorkers.setValue(settings.getMaxNetworkWorkers());
                    networkWorkersPanel.add(networkWorkers);
                    {
                        JLabel lblThisChangeRequires = new JLabel("This change requires application restart");
                        networkWorkersPanel.add(lblThisChangeRequires);
                    }
                }
                {
                    GridBagConstraints gbc_chckbxAutorefreshReceiversOn = new GridBagConstraints();
                    gbc_chckbxAutorefreshReceiversOn.fill = GridBagConstraints.HORIZONTAL;
                    gbc_chckbxAutorefreshReceiversOn.insets = new Insets(0, 0, 5, 0);
                    gbc_chckbxAutorefreshReceiversOn.gridx = 0;
                    gbc_chckbxAutorefreshReceiversOn.gridy = 1;
                    autoRefreshCheck.setSelected(settings.isAutoRefreshPrivateReceivers());
                    panel.add(autoRefreshCheck, gbc_chckbxAutorefreshReceiversOn);
                }
                {
                    GridBagConstraints gbc_chckbxAuto = new GridBagConstraints();
                    gbc_chckbxAuto.fill = GridBagConstraints.HORIZONTAL;
                    gbc_chckbxAuto.gridx = 0;
                    gbc_chckbxAuto.gridy = 2;
                    autoDownloadCheck.setSelected(settings.isAutoDownloadPublicReceivers());
                    panel.add(autoDownloadCheck, gbc_chckbxAuto);
                }
            }
        }
        {
            JPanel buttonPane = new JPanel();
            buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
            getContentPane().add(buttonPane, BorderLayout.SOUTH);
            {
                JButton saveButton = new JButton("Save");
                saveButton.addActionListener(e -> {

                    settings.setAutoDownloadPublicReceivers(autoDownloadCheck.isSelected());
                    settings.setAutoRefreshPrivateReceivers(autoRefreshCheck.isSelected());
                    settings.setMaxNetworkWorkers((int) networkWorkers.getValue());

                    dispose();
                });
                saveButton.setActionCommand("Save");
                buttonPane.add(saveButton);
                getRootPane().setDefaultButton(saveButton);
            }
            {
                JButton cancelButton = new JButton("Cancel");
                cancelButton.addActionListener(e -> dispose());
                cancelButton.setActionCommand("Cancel");
                buttonPane.add(cancelButton);
            }
        }
        pack();
        setLocationRelativeTo(parent);
    }

}
