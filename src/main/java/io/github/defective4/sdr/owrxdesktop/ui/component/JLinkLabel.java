package io.github.defective4.sdr.owrxdesktop.ui.component;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.util.Map;

import javax.swing.JLabel;

public class JLinkLabel extends JLabel {

    private final ActionListener ls;

    public JLinkLabel(String text, ActionListener ls) {
        super(text);
        this.ls = ls;
        setForeground(Color.decode("#00ABFF"));
        setFont(getFont().deriveFont(Map.of(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON)));
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ls.actionPerformed(null);
            }
        });
    }

}
