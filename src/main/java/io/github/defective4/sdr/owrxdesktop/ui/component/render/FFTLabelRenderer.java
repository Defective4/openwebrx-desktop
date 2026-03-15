package io.github.defective4.sdr.owrxdesktop.ui.component.render;

import java.awt.Color;
import java.awt.Component;
import java.awt.font.TextAttribute;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JTable;

import io.github.defective4.sdr.owrxdesktop.ui.BookmarksDialog.MergedLabel;

public class FFTLabelRenderer extends TableComponentRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
        Component cpt = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (value instanceof MergedLabel label && cpt instanceof JLabel textLabel) {
            textLabel.setText(label.label().name());
            textLabel.setFont(
                    textLabel.getFont().deriveFont(Map.of(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON)));
            textLabel.setForeground(label.label().activeColor());
        } else
            cpt.setForeground(Color.white);
        return cpt;
    }

}
