package io.github.defective4.sdr.owrxdesktop.ui.component;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

public class JLED extends JLabel {
    private static final Color[] BG_COLORS = new Color[] { Color.RED, null, Color.GREEN };
    private static final Color[] FG_COLORS = new Color[] { Color.WHITE, null, Color.BLACK };

    private final Color bg;
    private final Color fg;

    public JLED(String text) {
        super(" " + text + " ");
        bg = getBackground();
        fg = getForeground();
        setBorder(new EmptyBorder(2, 7, 2, 7));
    }

    public void setState(boolean state) {
        setState(state ? 1 : 0);
    }

    public void setState(int state) {
        if (state < -1) state = -1;
        state++;
        if (state >= FG_COLORS.length) state = FG_COLORS.length - 1;
        Color fg = FG_COLORS[state];
        Color bg = BG_COLORS[state];
        if (fg == null) fg = this.fg;
        if (bg == null) bg = this.bg;
        setForeground(fg);
        setBackground(bg);

        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        super.paintComponent(g);

        g.setColor(fg);
        g.drawLine(0, 0, getWidth(), 0);
        g.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
        g.drawLine(0, 0, 0, getHeight());
        g.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight());
    }
}
