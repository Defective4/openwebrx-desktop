package io.github.defective4.sdr.owrxdesktop.ui.component;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractButton;
import javax.swing.JTable;

import io.github.defective4.sdr.owrxdesktop.ui.component.render.TableComponentRenderer;

public class JComponentTable extends JTable {
    public JComponentTable() {
        setDefaultRenderer(Object.class, new TableComponentRenderer());
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Point point = e.getPoint();
                int row = rowAtPoint(point);
                int col = columnAtPoint(point);
                if (row >= 0 && col >= 0 && row < getRowCount() && col < getColumnCount()) {
                    Object value = getValueAt(row, col);
                    if (value instanceof AbstractButton btn) {
                        btn.doClick();
                        invalidate();
                        repaint();
                    }
                }
            }
        });
    }
}
