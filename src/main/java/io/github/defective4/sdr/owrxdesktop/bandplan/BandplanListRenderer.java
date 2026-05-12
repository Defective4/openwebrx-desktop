package io.github.defective4.sdr.owrxdesktop.bandplan;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

public class BandplanListRenderer extends DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
            boolean cellHasFocus) {
        Component cpt = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (cpt instanceof JLabel label) {
            if (value instanceof SerializedBandplan bandplan) {
                label.setText(bandplan.name());
            } else
                label.setText("<No imported bandplans>");
        }
        return cpt;
    }

}
