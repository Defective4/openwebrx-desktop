package io.github.defective4.sdr.owrxdesktop.ui.component;

import static java.awt.RenderingHints.*;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JComponent;

import io.github.defective4.sdr.owrxdesktop.ui.event.TuningListener;

public class FFTPanel extends JComponent {
    private static final Color BG = Color.decode("#1F1D1D");
    private static final Color FREQ_BAR = Color.decode("#282525");
    private static final Color LINE = Color.decode("#3F3B3B");
    private static final Color LINE_CENTER = Color.white;
    private static final Color SCOPE = new Color(255, 255, 255, 50);
    private static final Color TEXT_COLOR = Color.white;
    private static final Color TUNE = Color.red;

    private int bandwidth = 960000;
    private int centerFrequency = (int) 100e6f;
    private final List<TuningListener> listeners = new CopyOnWriteArrayList<>();
    private boolean mouseDown = false;
    private int mouseX = -1;

    private int mouseY = -1;

    private int offset = 0;
    private int scope = (int) 150e3f;
    private int tuningStep = (int) 50e3f;

    public FFTPanel() {
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                mouseX = -1;
                mouseY = -1;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                mouseDown = true;
                tune(calculateOffsetAtPoint(e.getX()));
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                updateMouseCoordinates(e);
                mouseDown = false;
                repaint();
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                tune(calculateOffsetAtPoint(e.getX()));
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                updateMouseCoordinates(e);
                repaint();
            }
        });
    }

    public boolean addListener(TuningListener listener) {
        return listeners.add(Objects.requireNonNull(listener));
    }

    public int getBandwidth() {
        return bandwidth;
    }

    public int getCenterFrequency() {
        return centerFrequency;
    }

    public List<TuningListener> getListeners() {
        return Collections.unmodifiableList(listeners);
    }

    public int getOffset() {
        return offset;
    }

    public int getScope() {
        return scope;
    }

    public int getTuningStep() {
        return tuningStep;
    }

    public boolean removeListener(TuningListener listener) {
        return listeners.remove(listener);
    }

    public void setBandwidth(int bandwidth) {
        this.bandwidth = bandwidth;
    }

    public void setCenterFrequency(int centerFrequency) {
        this.centerFrequency = centerFrequency;
    }

    public void setScope(int scope) {
        this.scope = scope;
    }

    public void setTuningStep(int tuningStep) {
        this.tuningStep = tuningStep;
    }

    public void tune(int offset) {
        this.offset = (int) Math.round(offset / (double) tuningStep) * tuningStep;
        int bound = bandwidth / 2;
        if (this.offset > bound) this.offset = (int) (Math.floor(bound / (double) tuningStep) * tuningStep);
        if (this.offset < -bound) this.offset = (int) (Math.ceil(-bound / (double) tuningStep) * tuningStep);
        repaint();
        listeners.forEach(ls -> ls.tuned(this.offset));
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g2 = (Graphics2D) graphics;
        g2.setColor(BG);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.setRenderingHint(KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2.setFont(g2.getFont().deriveFont(12f));

        g2.setColor(LINE);
        g2.fillRect(0, getLineHeight(), getWidth(), getHeight());
        g2.drawLine(0, getLineHeight(), getWidth(), getLineHeight());

        int center = getWidth() / 2;
        drawFrequencyLine(g2, center, LINE_CENTER);

        double pxPerHz = calculatePixelPerHerz();
        int step = calculateDrawingStep(pxPerHz);

        double x = center;
        while (x < getWidth()) {
            x += pxPerHz * step;
            drawFrequencyLine(g2, (int) x, LINE);
        }

        x = center;
        while (x > 0) {
            x -= pxPerHz * step;
            drawFrequencyLine(g2, (int) x, LINE);
        }

        x = (int) (pxPerHz * offset + center);

        g2.setColor(SCOPE);

        int start = (int) Math.round(x - calculatePixelPerHerz() * scope / 2);

        g2.fillRect(start, 0, (int) (calculatePixelPerHerz() * scope), getLineHeight());

        g2.setColor(TUNE);
        g2.drawLine((int) x, 0, (int) x, getLineHeight());

        g2.setColor(TEXT_COLOR);
        if (mouseX != -1 && mouseY != -1 && !mouseDown && mouseY < getLineHeight()) {
            String freq = getDisplayFrequencyAt(mouseX, 100, true);
            g2.drawString(freq, mouseX + 5, mouseY - 5);
        }
    }

    private int calculateHerzPerPixel() {
        return (int) (bandwidth / (double) getWidth());
    }

    private int calculateOffsetAtPoint(int x) {
        return Math.round(x * calculateHerzPerPixel() - bandwidth / 2);
    }

    private double calculatePixelPerHerz() {
        return getWidth() / (double) bandwidth;
    }

    private void drawFrequencyLine(Graphics2D g2, int x, Color color) {
        g2.setColor(color);
        g2.drawLine(x, 0, x, getLineHeight());

        g2.setColor(TEXT_COLOR);
        String displayFreq = getDisplayFrequencyAt(x, 10, false);

        FontMetrics metrics = g2.getFontMetrics();
        int width = metrics.stringWidth(displayFreq);
        int textX = x - width / 2;

        if (textX > 0 && textX + width < getWidth())
            g2.drawString(displayFreq, textX, getLineHeight() + metrics.getHeight());

        g2.setColor(BG);
    }

    private String getDisplayFrequencyAt(int x, double accuracy, boolean drawUnits) {
        int freq = calculateOffsetAtPoint(x) + centerFrequency;
        int divider = (int) 1e6f;

        StringBuilder str = new StringBuilder(
                Double.toString((int) Math.round(freq / (divider / accuracy)) / accuracy));
        if (drawUnits) str.append(" MHz");
        return str.toString();
    }

    private int getLineHeight() {
        return getHeight() - 24;
    }

    private void updateMouseCoordinates(MouseEvent e) {
        mouseX = Math.max(Math.min(getWidth(), e.getX()), 0);
        mouseY = Math.max(Math.min(getHeight(), e.getY()), 0);
    }

    private static int calculateDrawingStep(double pxPerHz) {
        int part = 3 - (int) Math.floor(pxPerHz * 1e4f / 2d);
        return (int) (Math.max(part, 1) * 100e3f);
    }
}
