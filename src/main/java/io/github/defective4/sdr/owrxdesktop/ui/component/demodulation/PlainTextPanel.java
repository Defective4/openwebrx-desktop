package io.github.defective4.sdr.owrxdesktop.ui.component.demodulation;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class PlainTextPanel extends JPanel {
    private final JTextArea textArea;

    public PlainTextPanel() {
        setLayout(new BorderLayout(0, 0));

        JScrollPane scrollPane = new JScrollPane();
        add(scrollPane);

        textArea = new JTextArea();
        textArea.setRows(8);
        textArea.setEditable(false);
        scrollPane.setViewportView(textArea);

        JPanel panel = new JPanel();
        add(panel, BorderLayout.SOUTH);
        FlowLayout flowLayout = (FlowLayout) panel.getLayout();
        flowLayout.setAlignment(FlowLayout.LEFT);

        JButton btnClear = new JButton("Clear");
        panel.add(btnClear);

        JButton btnCopyToClipboard = new JButton("Copy to clipboard");
        btnCopyToClipboard.addActionListener(e -> {
            StringSelection sel = new StringSelection(textArea.getText());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, sel);
        });
        panel.add(btnCopyToClipboard);
        btnClear.addActionListener(e -> textArea.setText(""));
    }

    public void appendText(String str) {
        textArea.setText(textArea.getText() + str);
    }
}
