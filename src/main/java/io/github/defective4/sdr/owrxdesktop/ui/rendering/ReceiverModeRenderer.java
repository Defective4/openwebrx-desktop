package io.github.defective4.sdr.owrxdesktop.ui.rendering;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

import io.github.defective4.sdr.owrxclient.model.ReceiverMode;

public class ReceiverModeRenderer extends DefaultListCellRenderer {

    public ReceiverModeRenderer() {}

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
            boolean cellHasFocus) {
        Component cpt = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (cpt instanceof JLabel label) {
            if (value != null) {
                label.setText(((ReceiverMode) value).name());
            } else
                label.setText("<none>");
        }
        return cpt;
    }

}
