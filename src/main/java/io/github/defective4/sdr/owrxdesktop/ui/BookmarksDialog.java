package io.github.defective4.sdr.owrxdesktop.ui;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import io.github.defective4.sdr.owrxdesktop.cache.ReceiverCache;
import io.github.defective4.sdr.owrxdesktop.ui.component.FFTLabel;
import io.github.defective4.sdr.owrxdesktop.ui.component.FFTLabelRenderer;
import io.github.defective4.sdr.owrxdesktop.ui.component.JFrequencySpinner.FrequencyFormatter;

public class BookmarksDialog extends JDialog {
    public static record MergedLabel(FFTLabel label, String profile) {

    }

    private MergedLabel label;

    private JTable table;

    private BookmarksDialog(ReceiverCache cache, Frame window) {
        super(window);
        setTitle("Bookmarks");
        setModal(true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(450, 400);
        setLocationRelativeTo(window);
        getContentPane().setLayout(new BorderLayout());
        JPanel contentPanel = new JPanel();
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        contentPanel.setLayout(new BorderLayout(0, 0));
        {
            JScrollPane scrollPane = new JScrollPane();
            contentPanel.add(scrollPane, BorderLayout.CENTER);
            {
                table = new JTable();
                DefaultTableModel model = new DefaultTableModel(new String[] { "Name", "Frequency", "Type" }, 0) {
                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }
                };
                table.setModel(model);
                table.getTableHeader().setReorderingAllowed(false);
                table.setCellSelectionEnabled(true);
                table.setDefaultRenderer(Object.class, new FFTLabelRenderer());
                scrollPane.setViewportView(table);

                MouseAdapter adapter = new MouseAdapter() {

                    private final Cursor DEFAULT = new Cursor(Cursor.DEFAULT_CURSOR);
                    private final Cursor POINTER = new Cursor(Cursor.HAND_CURSOR);

                    @Override
                    public void mouseClicked(MouseEvent e) {
                        int col = table.columnAtPoint(e.getPoint());
                        int row = table.rowAtPoint(e.getPoint());
                        if (col == 0 && row >= 0 && row < model.getRowCount()) {
                            label = (MergedLabel) table.getValueAt(row, col);
                            dispose();
                        }
                    }

                    @Override
                    public void mouseMoved(MouseEvent e) {
                        if (table.columnAtPoint(e.getPoint()) == 0)
                            table.setCursor(POINTER);
                        else
                            table.setCursor(DEFAULT);
                    }
                };

                table.addMouseListener(adapter);
                table.addMouseMotionListener(adapter);
                table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

                FrequencyFormatter fmt = new FrequencyFormatter();

                List<MergedLabel> sorted = cache.getLabels().entrySet().stream()
                        .flatMap(t -> t.getValue().stream().map(l -> new MergedLabel(l, t.getKey())))
                        .sorted((o1, o2) -> o2.label.freq() - o1.label.freq()).toList();

                for (MergedLabel label : sorted) {
                    try {
                        model.addRow(new Object[] { label, fmt.valueToString(label.label.freq()), label.label.type() });
                    } catch (Exception e) {
                        throw new IllegalStateException(e);
                    }
                }
            }
        }
        {
            JPanel buttonPane = new JPanel();
            buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
            getContentPane().add(buttonPane, BorderLayout.SOUTH);
            {
                JButton okButton = new JButton("OK");
                okButton.setActionCommand("OK");
                okButton.addActionListener(e -> dispose());
                buttonPane.add(okButton);
                getRootPane().setDefaultButton(okButton);
            }
        }
    }

    public static MergedLabel show(ReceiverCache cache, Frame window) {
        BookmarksDialog dialog = new BookmarksDialog(cache, window);
        dialog.setVisible(true);
        return dialog.label;
    }

}
