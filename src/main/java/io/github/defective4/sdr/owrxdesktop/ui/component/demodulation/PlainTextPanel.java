package io.github.defective4.sdr.owrxdesktop.ui.component.demodulation;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

public class PlainTextPanel extends JPanel {
    private final JTextArea textArea;

    public PlainTextPanel() {
        setBorder(new TitledBorder(null, "Plain text", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        GridBagLayout gbl_panel_1 = new GridBagLayout();
        gbl_panel_1.columnWidths = new int[] { 0, 0 };
        gbl_panel_1.rowHeights = new int[] { 0, 0, 0 };
        gbl_panel_1.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
        gbl_panel_1.rowWeights = new double[] { 1.0, 0.0, Double.MIN_VALUE };
        setLayout(gbl_panel_1);

        JScrollPane scrollPane = new JScrollPane();
        GridBagConstraints gbc_scrollPane = new GridBagConstraints();
        gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
        gbc_scrollPane.fill = GridBagConstraints.BOTH;
        gbc_scrollPane.gridx = 0;
        gbc_scrollPane.gridy = 0;
        add(scrollPane, gbc_scrollPane);

        textArea = new JTextArea();
        textArea.setEditable(false);
        scrollPane.setViewportView(textArea);

        JButton btnClear = new JButton("Clear");
        btnClear.addActionListener(e -> { textArea.setText(""); });
        GridBagConstraints gbc_btnClear = new GridBagConstraints();
        gbc_btnClear.anchor = GridBagConstraints.WEST;
        gbc_btnClear.gridx = 0;
        gbc_btnClear.gridy = 1;
        add(btnClear, gbc_btnClear);
    }

    public void appendText(String str) {
        textArea.setText(textArea.getText() + str);
    }
}
