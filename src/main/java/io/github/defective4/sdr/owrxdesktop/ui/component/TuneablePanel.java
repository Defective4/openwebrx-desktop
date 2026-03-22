package io.github.defective4.sdr.owrxdesktop.ui.component;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JComponent;

import io.github.defective4.sdr.owrxdesktop.ui.event.TuningListener;

public abstract class TuneablePanel extends JComponent implements FFTVisualizer {
    protected static final Color BG = Color.decode("#1F1D1D");
    protected static final Color TEXT_COLOR = Color.white;
    private static final Color SCOPE = new Color(255, 255, 255, 50);
    private static final Color TUNE = Color.red;
    protected int bandwidth = 968000;
    protected int centerFrequency = (int) 1e6f;
    protected boolean drawScope = true;
    protected boolean mouseDown = false;

    protected int mouseX = -1;
    protected int mouseY = -1;
    protected int offset = 0;
    protected int scopeLower = (int) -10e3f;
    protected int scopeUpper = (int) 10e3f;

    protected boolean tuningReady;

    protected int tuningStep = (int) 1e3f;
    private boolean drawFrequencyLabel = true;

    private final List<TuningListener> listeners = new CopyOnWriteArrayList<>();
    private boolean symmetricalScope = true;

    public TuneablePanel() {
        MouseAdapter adapter = new MouseAdapter() {

            private final Cursor DEFAULT = new Cursor(Cursor.DEFAULT_CURSOR);

            private int dragX = 0;

            private final Cursor E_RESIZE = new Cursor(Cursor.W_RESIZE_CURSOR);
            private boolean noinput;
            private final Cursor RESIZE = new Cursor(Cursor.MOVE_CURSOR);
            private int scopeMode = 0;

            private boolean tuneMode = false;
            private final Cursor W_RESIZE = new Cursor(Cursor.E_RESIZE_CURSOR);

            @Override
            public void mouseDragged(MouseEvent e) {
                if (noinput) return;
                if (tuneMode)
                    tune(calculateOffsetAtPoint(e.getX()));
                else if (scopeMode != 0 && e.getY() <= getLineHeight()) {
                    int center = e.getX() - (int) (getWidth() / 2 + offset * calculatePixelPerHerz());
                    int hzMod = (int) (calculateHerzPerPixel() * center);
                    if (scopeMode > 0) {
                        scopeUpper = hzMod;
                        if (symmetricalScope) scopeLower = -hzMod;
                    } else {
                        scopeLower = hzMod;
                        if (symmetricalScope) scopeUpper = -hzMod;
                    }
                    listeners.forEach(ls -> ls.scopeChanged(scopeLower, scopeUpper));
                    repaint();
                } else {
                    int xMod = e.getXOnScreen() - dragX;
                    Rectangle bounds = getBounds();
                    setBounds(bounds.x + xMod, bounds.y, bounds.width, bounds.height);
                    dragX = e.getXOnScreen();
                    listeners.forEach(ls -> ls.zoomChanged(bounds.x, bounds.width));
                }
                updateMouseCoordinates(e);
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
                if (e.getY() > getLineHeight()) {
                    setCursor(RESIZE);
                } else {
                    int scopeArea = getScopeArea(e.getX());
                    if (scopeArea != 0) {
                        setCursor(scopeArea > 0 ? W_RESIZE : E_RESIZE);
                    } else
                        setCursor(DEFAULT);
                }
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                noinput = false;
                if (e.getButton() != MouseEvent.BUTTON1) {
                    noinput = true;
                }
                mouseDown = true;
                tuneMode = e.getY() <= getLineHeight() && e.getButton() == MouseEvent.BUTTON1;
                scopeMode = getScopeArea(e.getX());
                if (tuneMode && scopeMode != 0) {
                    dragX = e.getXOnScreen();
                    tuneMode = false;
                } else if (tuneMode) {
                    tune(calculateOffsetAtPoint(e.getX()));
                } else if (e.getY() > getLineHeight()) {
                    dragX = e.getXOnScreen();
                    if (e.getButton() == MouseEvent.BUTTON3) {
                        setBounds(0, getY(), getParent().getWidth(), getHeight());
                        listeners.forEach(ls -> ls.zoomChanged(0, getParent().getWidth()));
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                updateMouseCoordinates(e);
                mouseDown = false;
                repaint();
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (e.getY() <= getLineHeight())
                    tune(offset + -e.getWheelRotation() * tuningStep);
                else {
                    double ratio = e.getX() / (double) getWidth();
                    int width = Math.max(getWidth() + getWidth() / 2 * -e.getWheelRotation(), getParent().getWidth());
                    int x = getBounds().x - (int) (getWidth() / 2 * ratio) * -e.getWheelRotation();
                    if (width == getParent().getWidth()) x = 0;
                    if (x > 0) x = 0;
                    int fx = x;
                    setBounds(x, getY(), width, getHeight());
                    listeners.forEach(ls -> ls.zoomChanged(fx, width));
                }
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

    @Override
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

    public boolean isDrawFrequencyLabel() {
        return drawFrequencyLabel;
    }

    public boolean isSymmetricalScope() {
        return symmetricalScope;
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

    public void setDrawFrequencyLabel(boolean drawFrequencyLabel) {
        this.drawFrequencyLabel = drawFrequencyLabel;
    }

    public void setScopeLower(int scopeLower) {
        this.scopeLower = scopeLower;
        repaint();
    }

    public void setScopeUpper(int scopeUpper) {
        this.scopeUpper = scopeUpper;
        repaint();
    }

    public void setSymmetricalScope(boolean symmetricalScope) {
        this.symmetricalScope = symmetricalScope;
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
        tune(offset, true, true);
    }

    public void tune(int offset, boolean fireEvents, boolean snap) {
        if (!tuningReady) {
            repaint();
            return;
        }
        if (snap)
            this.offset = (int) Math.round(offset / (double) tuningStep) * tuningStep;
        else
            this.offset = offset;
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
    protected void paintComponent(Graphics g2) {
        double pxPerHz = calculatePixelPerHerz();
        int center = getWidth() / 2;

        int x = (int) (pxPerHz * offset + center);
        if (tuningReady) {
            if (drawScope) {
                g2.setColor(SCOPE);
                int start = (int) Math.round(x - calculatePixelPerHerz() * -scopeLower);

                g2.fillRect(start, 0, (int) (calculatePixelPerHerz() * (-scopeLower + scopeUpper)),
                        getLineHeight() + 1);

                g2.setColor(TUNE);
                g2.drawLine(x, 0, x, getLineHeight());
            }

            if (drawFrequencyLabel) {
                g2.setColor(TEXT_COLOR);
                if (mouseX != -1 && mouseY != -1 && (!drawScope || !mouseDown) && mouseY < getLineHeight()) {
                    String freq = getDisplayFrequencyAt(mouseX, 100, true);
                    g2.drawString(freq, mouseX + 5, mouseY - 5);
                }
            }
        }
    }

    private double calculateHerzPerPixel() {
        return bandwidth / (double) getWidth();
    }

    private int calculateOffsetAtPoint(int x) {
        return (int) Math.round(x * calculateHerzPerPixel() - bandwidth / 2);
    }

    private int getScopeArea(int x) {
        int center = x - (int) (getWidth() / 2 + offset * calculatePixelPerHerz());
        boolean lower = false;
        boolean upper = false;
        if (Math.abs(center - scopeLower * calculatePixelPerHerz()) < 10) {
            lower = true;
        }
        if (Math.abs(center - scopeUpper * calculatePixelPerHerz()) < 10) {
            upper = true;
        }
        if (lower == upper)
            return 0;
        else if (lower)
            return -1;
        else
            return 1;
    }

    private void updateMouseCoordinates(MouseEvent e) {
        mouseX = Math.max(Math.min(getWidth(), e.getX()), 0);
        mouseY = Math.max(Math.min(getHeight(), e.getY()), 0);
    }

}
