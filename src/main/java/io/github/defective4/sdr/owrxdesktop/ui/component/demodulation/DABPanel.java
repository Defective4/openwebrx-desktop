package io.github.defective4.sdr.owrxdesktop.ui.component.demodulation;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;

import io.github.defective4.sdr.owrxclient.model.metadata.DABMetadata;

public class DABPanel extends JPanel {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    private JButton btnSelect;
    private final JTextField clockField;
    private String eid = "0x0";
    private String elabel = "";

    private final JTextField ensLabel;

    private final JLabel lblClock;

    private final JLabel lblProgramme;
    private final JComboBox<Map.Entry<String, String>> serviceBox;

    public DABPanel(Consumer<Integer> serviceConsumer) {
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[] { 0, 0, 0 };
        gridBagLayout.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0 };
        gridBagLayout.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
        gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
        setLayout(gridBagLayout);

        JLabel lblEnsembleId = new JLabel("Ensemble");
        GridBagConstraints gbc_lblEnsembleId = new GridBagConstraints();
        gbc_lblEnsembleId.insets = new Insets(0, 0, 5, 5);
        gbc_lblEnsembleId.anchor = GridBagConstraints.EAST;
        gbc_lblEnsembleId.gridx = 0;
        gbc_lblEnsembleId.gridy = 0;
        add(lblEnsembleId, gbc_lblEnsembleId);

        ensLabel = new JTextField();
        ensLabel.setEditable(false);
        GridBagConstraints gbc_textField = new GridBagConstraints();
        gbc_textField.insets = new Insets(0, 0, 5, 0);
        gbc_textField.fill = GridBagConstraints.HORIZONTAL;
        gbc_textField.gridx = 1;
        gbc_textField.gridy = 0;
        add(ensLabel, gbc_textField);
        ensLabel.setColumns(10);

        JSeparator separator = new JSeparator();
        GridBagConstraints gbc_separator = new GridBagConstraints();
        gbc_separator.insets = new Insets(0, 0, 5, 0);
        gbc_separator.gridx = 1;
        gbc_separator.gridy = 1;
        add(separator, gbc_separator);

        lblProgramme = new JLabel("Programme");
        GridBagConstraints gbc_lblProgramme = new GridBagConstraints();
        gbc_lblProgramme.anchor = GridBagConstraints.EAST;
        gbc_lblProgramme.insets = new Insets(0, 0, 5, 5);
        gbc_lblProgramme.gridx = 0;
        gbc_lblProgramme.gridy = 2;
        add(lblProgramme, gbc_lblProgramme);

        serviceBox = new JComboBox<>();
        serviceBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected,
                        cellHasFocus);
                if (value != null) label.setText(((Map.Entry<String, String>) value).getValue());
                return label;
            }
        });
        GridBagConstraints gbc_comboBox = new GridBagConstraints();
        gbc_comboBox.insets = new Insets(0, 0, 5, 0);
        gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
        gbc_comboBox.gridx = 1;
        gbc_comboBox.gridy = 2;
        add(serviceBox, gbc_comboBox);

        btnSelect = new JButton("Select");
        GridBagConstraints gbc_btnSelect = new GridBagConstraints();
        gbc_btnSelect.fill = GridBagConstraints.HORIZONTAL;
        gbc_btnSelect.insets = new Insets(0, 0, 5, 0);
        gbc_btnSelect.gridx = 1;
        gbc_btnSelect.gridy = 3;
        add(btnSelect, gbc_btnSelect);

        btnSelect.addActionListener(e -> {
            Map.Entry<String, String> entry = (Entry<String, String>) serviceBox.getSelectedItem();
            if (entry != null) serviceConsumer.accept(Integer.parseInt(entry.getKey()));
        });

        JSeparator separator_1 = new JSeparator();
        GridBagConstraints gbc_separator_1 = new GridBagConstraints();
        gbc_separator_1.insets = new Insets(0, 0, 5, 0);
        gbc_separator_1.gridx = 1;
        gbc_separator_1.gridy = 4;
        add(separator_1, gbc_separator_1);

        lblClock = new JLabel("Clock");
        GridBagConstraints gbc_lblClock = new GridBagConstraints();
        gbc_lblClock.anchor = GridBagConstraints.EAST;
        gbc_lblClock.insets = new Insets(0, 0, 0, 5);
        gbc_lblClock.gridx = 0;
        gbc_lblClock.gridy = 5;
        add(lblClock, gbc_lblClock);

        clockField = new JTextField();
        clockField.setText("--.--.---- --:--:--");
        clockField.setEditable(false);
        GridBagConstraints gbc_textField_2 = new GridBagConstraints();
        gbc_textField_2.fill = GridBagConstraints.HORIZONTAL;
        gbc_textField_2.gridx = 1;
        gbc_textField_2.gridy = 5;
        add(clockField, gbc_textField_2);
        clockField.setColumns(10);
    }

    public void setData(DABMetadata metadata) {
        metadata.getEnsembleLabel().ifPresent(label -> elabel = label);
        metadata.getEnsembleID().ifPresent(id -> eid = "0x" + Integer.toHexString(id));
        ensLabel.setText("%s (%s)".formatted(elabel, eid));
        metadata.getTimestamp().ifPresent(ts -> clockField.setText(
                FORMATTER.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(ts * 1000), ZoneId.systemDefault()))));
        metadata.getProgrammes().ifPresent(map -> {
            serviceBox.removeAllItems();
            map.entrySet().forEach(serviceBox::addItem);
        });
    }
}
