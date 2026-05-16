package io.github.defective4.sdr.owrxdesktop.ui.component.demodulation;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import io.github.defective4.sdr.owrxclient.model.demod.FTMessage;
import io.github.defective4.sdr.owrxdesktop.ui.component.JFrequencySpinner.FrequencyFormatter;

public class FTPanel extends JPanel {

    private static final DateFormat FORMAT = new SimpleDateFormat("HH:mm:ss");
    private static final FrequencyFormatter FORMATTER = new FrequencyFormatter();

    private final DefaultTableModel model = new DefaultTableModel(new Object[][] {},
            new String[] { "Mode", "Time", "Freq", "Message", "Db" }) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    public FTPanel() {
        setLayout(new BorderLayout(0, 0));

        JScrollPane scrollPane = new JScrollPane();
        add(scrollPane, BorderLayout.CENTER);

        JPanel panel_2 = new JPanel();
        scrollPane.setViewportView(panel_2);
        panel_2.setLayout(new BorderLayout(0, 0));

        JTable table = new JTable();
        table.setShowHorizontalLines(true);
        table.setRowSelectionAllowed(false);

        table.setModel(model);
        table.getTableHeader().setReorderingAllowed(false);
        scrollPane.setColumnHeaderView(table.getTableHeader());
        TableColumnModel columnModel = table.getTableHeader().getColumnModel();
        for (int i = 0; i < columnModel.getColumnCount(); i++) columnModel.getColumn(i).setPreferredWidth(0);
        panel_2.add(table, BorderLayout.CENTER);

        JPanel panel = new JPanel();
        FlowLayout flowLayout = (FlowLayout) panel.getLayout();
        flowLayout.setAlignment(FlowLayout.LEFT);
        panel_2.add(panel, BorderLayout.SOUTH);

        JButton btnClear = new JButton("Clear");
        btnClear.addActionListener(e -> { model.setRowCount(0); });
        panel.add(btnClear);

        JButton btnCopyAsCsv = new JButton("Copy as CSV");
        btnCopyAsCsv.addActionListener(e -> {
            StringWriter stringWriter = new StringWriter();
            try (PrintWriter pw = new PrintWriter(stringWriter)) {
                TableColumnModel columns = table.getTableHeader().getColumnModel();
                String[] vals;

                vals = new String[columns.getColumnCount()];
                for (int i = 0; i < vals.length; i++) {
                    vals[i] = (String) columns.getColumn(i).getHeaderValue();
                }

                pw.println(String.join(", ", vals));

                int rows = model.getRowCount();
                for (int row = 0; row < rows; row++) {
                    for (int col = 0; col < vals.length; col++) {
                        vals[col] = (String) model.getValueAt(row, col);
                    }
                    pw.println(String.join(", ", vals));
                }
            }

            StringSelection sel = new StringSelection(stringWriter.toString());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, sel);
        });
        panel.add(btnCopyAsCsv);
    }

    public void insertMessage(FTMessage msg, int centerFreq) {
        try {
            model.insertRow(0,
                    new String[] { msg.getMode(), FORMAT.format(new Date(msg.getTimestamp())),
                            FORMATTER.valueToString(msg.getFrequency() - centerFreq), msg.getMessage(),
                            Double.toString(msg.getDb()) });
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
