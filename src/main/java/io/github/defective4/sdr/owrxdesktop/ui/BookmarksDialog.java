package io.github.defective4.sdr.owrxdesktop.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

import io.github.defective4.sdr.owrxdesktop.cache.ReceiverCache;
import io.github.defective4.sdr.owrxdesktop.ui.component.FFTLabel;
import io.github.defective4.sdr.owrxdesktop.ui.component.JComponentTable;
import io.github.defective4.sdr.owrxdesktop.ui.component.JFrequencySpinner.FrequencyFormatter;
import io.github.defective4.sdr.owrxdesktop.ui.component.render.FFTLabelRenderer;

public class BookmarksDialog extends JDialog {
    public static record MergedLabel(FFTLabel label, String profile) {

    }

    private static final int CHECKBOX_COLUMN = 0;

    private static final String CHECKBOX_COLUMN_ID = "selall";

    private static final int NAME_COLUMN = 1;

    private final JButton btnDelete = new JButton("Delete selected");

    private MergedLabel label;

    private final JTable table = new JComponentTable();

    private BookmarksDialog(ReceiverCache cache, ReceiverWindow window, String profile) {
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

                DefaultTableModel model = new DefaultTableModel(
                        new String[] { CHECKBOX_COLUMN_ID, "Name", "Frequency", "Type" }, 0) {
                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }
                };

                JCheckBox selectAllCheck = new JCheckBox();
                selectAllCheck.setBackground(new Color(0, 0, 0, 0));

                table.setModel(model);
                JTableHeader header = table.getTableHeader();
                header.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        int col = header.columnAtPoint(e.getPoint());
                        if (col == CHECKBOX_COLUMN) {
                            selectAllCheck.doClick();
                            header.invalidate();
                            header.repaint();
                        }
                    }
                });
                header.setReorderingAllowed(false);
                table.setCellSelectionEnabled(true);
                table.setDefaultRenderer(Object.class, new FFTLabelRenderer());
                TableColumn column = table.getColumn(CHECKBOX_COLUMN_ID);
                column.setMaxWidth(24);
                column.setResizable(false);
                table.getColumn(CHECKBOX_COLUMN_ID).setHeaderRenderer(new DefaultTableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                            boolean hasFocus, int row, int column) {
                        return selectAllCheck;
                    }
                });
                scrollPane.setViewportView(table);

                MouseAdapter adapter = new MouseAdapter() {

                    private final Cursor DEFAULT = new Cursor(Cursor.DEFAULT_CURSOR);
                    private final Cursor POINTER = new Cursor(Cursor.HAND_CURSOR);

                    @Override
                    public void mouseClicked(MouseEvent e) {
                        int col = table.columnAtPoint(e.getPoint());
                        int row = table.rowAtPoint(e.getPoint());
                        if (col == NAME_COLUMN && row >= 0 && row < model.getRowCount()) {
                            label = (MergedLabel) table.getValueAt(row, col);
                            dispose();
                        }
                    }

                    @Override
                    public void mouseMoved(MouseEvent e) {
                        if (table.columnAtPoint(e.getPoint()) == 1)
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

                ActionListener checkListener = e -> {
                    boolean allChecked = true;
                    boolean anyChecked = false;
                    int rows = table.getRowCount();
                    for (int i = 0; i < rows; i++) {
                        JCheckBox check = (JCheckBox) table.getValueAt(i, CHECKBOX_COLUMN);
                        if (!check.isEnabled()) continue;
                        if (check.isSelected()) anyChecked = true;
                        if (!check.isSelected()) allChecked = false;
                    }
                    selectAllCheck.setSelected(allChecked);
                    btnDelete.setEnabled(anyChecked);
                    header.repaint();
                };

                for (MergedLabel label : sorted) {
                    try {
                        JCheckBox checkBox = new JCheckBox();
                        checkBox.setEnabled(!label.profile().equals(profile));
                        checkBox.addActionListener(checkListener);
                        checkBox.setBackground(new Color(0, 0, 0, 0));

                        model.addRow(new Object[] { checkBox, label, fmt.valueToString(label.label.freq()),
                                label.label.type() });
                    } catch (Exception e) {
                        throw new IllegalStateException(e);
                    }
                }

                selectAllCheck.addActionListener(e -> {
                    int rows = table.getRowCount();
                    boolean anySelected = false;
                    for (int i = 0; i < rows; i++) {
                        JCheckBox check = (JCheckBox) table.getValueAt(i, CHECKBOX_COLUMN);
                        check.setSelected(check.isEnabled() && selectAllCheck.isSelected());
                        if (check.isSelected()) anySelected = true;
                        table.repaint();
                    }
                    btnDelete.setEnabled(anySelected);
                });
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
                btnDelete.setEnabled(false);
                btnDelete.setForeground(new Color(255, 155, 155));
                btnDelete.addActionListener(e -> {
                    if (JOptionPane.showOptionDialog(this, new String[] {
                            "Do you want to delete the selected bookmarks?",
                            "Keep in mind, that server bookmarks and dial frequencies may be restored once you switch back to their respective profiles" },
                            "Deleting bookmarks", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null,
                            null) == JOptionPane.YES_OPTION) {
                        List<MergedLabel> toDelete = new ArrayList<>();
                        int rows = table.getRowCount();
                        for (int i = 0; i < rows; i++) {
                            JCheckBox check = (JCheckBox) table.getValueAt(i, CHECKBOX_COLUMN);
                            if (check.isSelected()) {
                                MergedLabel label = (MergedLabel) table.getValueAt(i, NAME_COLUMN);
                                toDelete.add(label);
                            }
                        }
                        toDelete.forEach(label -> cache.removeLabel(label.profile(), label.label()));
                        dispose();
                        show(cache, window, profile);
                    }
                });
                JButton newBookmark = new JButton("New bookmark");

                newBookmark.addActionListener(e -> {
                    BookmarkEditorDialog.show(BookmarksDialog.this, window.getAnalogModes(), window.getDigitalModes(),
                            window.getCenterFrequency(), window.getPrimaryMode(),
                            window.getSecondaryMode().orElse(null));
                });

                buttonPane.add(newBookmark);
                buttonPane.add(btnDelete);
                buttonPane.add(okButton);
                getRootPane().setDefaultButton(okButton);
            }
        }
    }

    public static MergedLabel show(ReceiverCache cache, ReceiverWindow window, String profile) {
        BookmarksDialog dialog = new BookmarksDialog(cache, window, profile);
        dialog.setVisible(true);
        return dialog.label;
    }

}
