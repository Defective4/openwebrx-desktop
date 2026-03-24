package io.github.defective4.sdr.owrxdesktop.ui.component;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.image.BufferedImage;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import io.github.defective4.sdr.owrxdesktop.application.ReceiverEntry;

public class ReceiverEntryComponent extends JPanel {
    private final ReceiverEntry entry;
    private final BufferedImage placeholder;
    private final JImageViewer receiverIcon = new JImageViewer();
    private final JLabel receiverName = new JLabel("");
    private final JLabel receiverSoftware = new JLabel("");

    public ReceiverEntryComponent(ReceiverEntry entry, BufferedImage placeholder) {
        this.entry = entry;
        this.placeholder = placeholder;
        setBorder(new EmptyBorder(5, 5, 5, 5));
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[] { 0, 0, 0 };
        gridBagLayout.rowHeights = new int[] { 0, 0 };
        gridBagLayout.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
        gridBagLayout.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
        setLayout(gridBagLayout);
        {
            receiverIcon.setImage(placeholder);
            receiverIcon.setPreferredSize(new Dimension(96, 96));
            GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
            gbc_lblNewLabel.fill = GridBagConstraints.VERTICAL;
            gbc_lblNewLabel.insets = new Insets(0, 0, 0, 5);
            gbc_lblNewLabel.gridx = 0;
            gbc_lblNewLabel.gridy = 0;
            add(receiverIcon, gbc_lblNewLabel);
        }

        JPanel panel = new JPanel();
        GridBagConstraints gbc_panel = new GridBagConstraints();
        gbc_panel.anchor = GridBagConstraints.WEST;
        gbc_panel.fill = GridBagConstraints.VERTICAL;
        gbc_panel.gridx = 1;
        gbc_panel.gridy = 0;
        add(panel, gbc_panel);
        GridBagLayout gbl_panel = new GridBagLayout();
        gbl_panel.columnWidths = new int[] { 0, 0 };
        gbl_panel.rowHeights = new int[] { 0, 0, 0 };
        gbl_panel.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
        gbl_panel.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
        panel.setLayout(gbl_panel);
        {
            receiverName.setFont(receiverName.getFont().deriveFont(19f));
            GridBagConstraints gbc_receiverName = new GridBagConstraints();
            gbc_receiverName.insets = new Insets(0, 0, 5, 0);
            gbc_receiverName.anchor = GridBagConstraints.NORTHWEST;
            gbc_receiverName.gridx = 0;
            gbc_receiverName.gridy = 0;
            panel.add(receiverName, gbc_receiverName);
        }
        {
            GridBagConstraints gbc_lblSoftware = new GridBagConstraints();
            gbc_lblSoftware.anchor = GridBagConstraints.WEST;
            gbc_lblSoftware.gridx = 0;
            gbc_lblSoftware.gridy = 1;
            panel.add(receiverSoftware, gbc_lblSoftware);
        }

        updateEntry();
    }

    public synchronized void updateEntry() {
        entry.getReceiverData().ifPresentOrElse(data -> {
            receiverName.setText(data.receiver().name());
            receiverSoftware.setText(data.version());
        }, () -> {
            if (entry.isQuerying()) {
                receiverName.setText("<Loading>");
                receiverSoftware.setText("");
            } else {
                Exception e = entry.getQueryException();
                if (e != null) {
                    receiverName.setText("<Error>");
                    receiverSoftware.setText(e.toString());
                } else {
                    receiverName.setText("<No name>");
                    receiverSoftware.setText("Unknown");
                }
            }
        });

        entry.getReceiverImage().ifPresentOrElse(image -> receiverIcon.setImage(image), () -> {
            receiverIcon.setImage(placeholder);
        });

        invalidate();
        repaint();
    }
}
