package io.github.defective4.sdr.owrxdesktop.ui.component.demodulation;

import java.awt.BorderLayout;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;
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
        setBorder(new TitledBorder(null, "FT", TitledBorder.LEADING, TitledBorder.TOP, null, null));
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
        for (int x = 0; x < 10; x++) {
            model.addRow(new String[0]);
        }
    }

    public void insertMessage(FTMessage msg, int centerFreq) {
        try {
            if (model.getValueAt(model.getRowCount() - 1, 0) == null) model.removeRow(model.getRowCount() - 1);
            model.insertRow(0,
                    new String[] { msg.getMode(), FORMAT.format(new Date(msg.getTimestamp())),
                            FORMATTER.valueToString(msg.getFrequency() - centerFreq), msg.getMessage(),
                            Double.toString(msg.getDb()) });
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
