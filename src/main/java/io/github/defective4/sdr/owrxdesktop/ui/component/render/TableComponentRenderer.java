package io.github.defective4.sdr.owrxdesktop.ui.component.render;

import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class TableComponentRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
        return value instanceof JComponent cpt ? cpt
                : super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    }

}
