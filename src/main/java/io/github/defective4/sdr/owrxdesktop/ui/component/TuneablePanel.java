package io.github.defective4.sdr.owrxdesktop.ui.component;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JComponent;

import io.github.defective4.sdr.owrxdesktop.ui.event.TuningListener;

public abstract class TuneablePanel extends JComponent {
    protected static final Color BG = Color.decode("#1F1D1D");
    protected static final Color TEXT_COLOR = Color.white;
    private static final Color SCOPE = new Color(255, 255, 255, 50);
    private static final Color TUNE = Color.red;
    protected int bandwidth = 968000;
    protected int centerFrequency = (int) 1e6f;
    protected boolean mouseDown = false;

    protected int mouseX = -1;
    protected int mouseY = -1;
    protected int offset = 0;
    protected int scopeLower = (int) -10e3f;
    protected int scopeUpper = (int) 10e3f;

    protected boolean tuningReady;

    protected int tuningStep = (int) 1e3f;

    private final List<TuningListener> listeners = new CopyOnWriteArrayList<>();

    public TuneablePanel() {
        MouseAdapter adapter = new MouseAdapter() {

            @Override
            public void mouseDragged(MouseEvent e) {
                tune(calculateOffsetAtPoint(e.getX()));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                mouseX = -1;
                mouseY = -1;
                repaint();
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                updateMouseCoordinates(e);
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

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (e.getY() < getLineHeight()) tune(offset + -e.getWheelRotation() * tuningStep);
            }
        };

        addMouseListener(adapter);
        addMouseMotionListener(adapter);
        addMouseWheelListener(adapter);
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

    public abstract int getLineHeight();

    public List<TuningListener> getListeners() {
        return Collections.unmodifiableList(listeners);
    }

    public int getScopeLower() {
        return scopeLower;
    }

    public int getScopeUpper() {
        return scopeUpper;
    }

    public int getTuningStep() {
        return tuningStep;
    }

    public boolean isTuningReady() {
        return tuningReady;
    }

    public boolean removeListener(TuningListener listener) {
        return listeners.remove(listener);
    }

    public void setBandwidth(int bandwidth) {
        this.bandwidth = bandwidth;
        repaint();
    }

    public void setCenterFrequency(int centerFrequency) {
        this.centerFrequency = centerFrequency;
        repaint();
    }

    public void setScopeLower(int scopeLower) {
        this.scopeLower = scopeLower;
        repaint();
    }

    public void setScopeUpper(int scopeUpper) {
        this.scopeUpper = scopeUpper;
        repaint();
    }

    public void setTuningReady(boolean tuningReady) {
        this.tuningReady = tuningReady;
        repaint();
    }

    public void setTuningStep(int tuningStep) {
        this.tuningStep = tuningStep;
        repaint();
    }

    public void tune(int offset) {
        tune(offset, true);
    }

    public void tune(int offset, boolean fireEvents) {
        if (!tuningReady) {
            repaint();
            return;
        }
        this.offset = (int) Math.round(offset / (double) tuningStep) * tuningStep;
        int bound = bandwidth / 2;
        if (this.offset > bound) this.offset = (int) (Math.floor(bound / (double) tuningStep) * tuningStep);
        if (this.offset < -bound) this.offset = (int) (Math.ceil(-bound / (double) tuningStep) * tuningStep);
        repaint();
        if (fireEvents) listeners.forEach(ls -> ls.tuned(this.offset));
    }

    protected double calculatePixelPerHerz() {
        return getWidth() / (double) bandwidth;
    }

    protected String getDisplayFrequencyAt(int x, double accuracy, boolean drawUnits) {
        int freq = calculateOffsetAtPoint(x) + centerFrequency;
        int divider = (int) 1e6f;

        StringBuilder str = new StringBuilder(
                Double.toString((int) Math.round(freq / (divider / accuracy)) / accuracy));
        if (drawUnits) str.append(" MHz");
        return str.toString();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics g2 = graphics;

        double pxPerHz = calculatePixelPerHerz();
        int center = getWidth() / 2;

        int x = (int) (pxPerHz * offset + center);
        if (tuningReady) {
            g2.setColor(SCOPE);
            int start = (int) Math.round(x - calculatePixelPerHerz() * -scopeLower);

            g2.fillRect(start, 0, (int) (calculatePixelPerHerz() * (-scopeLower + scopeUpper)), getLineHeight());

            g2.setColor(TUNE);
            g2.drawLine(x, 0, x, getLineHeight());

            g2.setColor(TEXT_COLOR);
            if (mouseX != -1 && mouseY != -1 && !mouseDown && mouseY < getLineHeight()) {
                String freq = getDisplayFrequencyAt(mouseX, 100, true);
                g2.drawString(freq, mouseX + 5, mouseY - 5);
            }
        }
    }

    private int calculateHerzPerPixel() {
        return (int) (bandwidth / (double) getWidth());
    }

    private int calculateOffsetAtPoint(int x) {
        return Math.round(x * calculateHerzPerPixel() - bandwidth / 2);
    }

    private void updateMouseCoordinates(MouseEvent e) {
        mouseX = Math.max(Math.min(getWidth(), e.getX()), 0);
        mouseY = Math.max(Math.min(getHeight(), e.getY()), 0);
    }

}
