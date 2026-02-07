package io.github.defective4.sdr.owrxdesktop.ui.component;

import static java.awt.RenderingHints.*;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JComponent;

import io.github.defective4.sdr.owrxdesktop.ui.event.TuningListener;

public class FFTPanel extends JComponent {
    private static final Color BG = Color.decode("#1F1D1D");
    private static final Color FFT_COLOR = Color.white;
    private static final Color FREQ_BAR = Color.decode("#282525");
    private static final Color LINE = Color.decode("#3F3B3B");
    private static final Color LINE_CENTER = Color.white;
    private static final Color SCOPE = new Color(255, 255, 255, 50);
    private static final Color TEXT_COLOR = Color.white;
    private static final Color TUNE = Color.red;

    private int bandwidth = 968000;
    private int centerFrequency = (int) 1e6f;

    private float[] fft = new float[0];

    private final Object fftLock = new Object();

    private float fftMax = -20;

    private float fftMin = -88;

    private final List<TuningListener> listeners = new CopyOnWriteArrayList<>();
    private boolean mouseDown = false;
    private int mouseX = -1;
    private int mouseY = -1;

    private int offset = 0;

    private int scopeLower = (int) -10e3f;

    private int scopeUpper = (int) 10e3f;
    private boolean tuningReady;
    private int tuningStep = (int) 1e3f;

    public FFTPanel() {
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

    public void drawFFT(float[] fft) {
        synchronized (fftLock) {
            this.fft = fft;
        }
        repaint();
    }

    public int getBandwidth() {
        return bandwidth;
    }

    public int getCenterFrequency() {
        return centerFrequency;
    }

    public float getFftMax() {
        return fftMax;
    }

    public float getFftMin() {
        return fftMin;
    }

    public List<TuningListener> getListeners() {
        return Collections.unmodifiableList(listeners);
    }

    public int getOffset() {
        return offset;
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

    public void setFFTMax(float fftMax) {
        this.fftMax = fftMax;
    }

    public void setFFTMin(float fftMin) {
        this.fftMin = fftMin;
    }

    public void setScopeLower(int scopeLower) {
        this.scopeLower = scopeLower;
    }

    public void setScopeUpper(int scopeUpper) {
        this.scopeUpper = scopeUpper;
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

        double dy = 0;

        double dbStep = calculatePixelPerDb() * 10d;

        dy += dbStep;
        while (dy < getLineHeight()) {
            drawSignalLine(g2, (int) Math.round(dy), LINE);
            dy += dbStep;
        }

        x = (int) (pxPerHz * offset + center);

        g2.setColor(FFT_COLOR);

        synchronized (fftLock) {
            int prevX = 0;
            float prevVal = -1;
            if (fft.length > 0) for (int i = 0; i < fft.length; i++) {
                int ix = (int) Math.round(i / (double) fft.length * getWidth());
                float range = fftMax - fftMin;
                float val = fftMax - fft[i];
                double r = val / range;

                int y = (int) (getLineHeight() * r);

                if (prevVal != -1) {
                    g2.drawLine(prevX, (int) prevVal, ix, y);
                }
                prevVal = y;
                prevX = ix;
            }
        }

        if (tuningReady) {
            g2.setColor(SCOPE);
            int start = (int) Math.round(x - calculatePixelPerHerz() * -scopeLower);

            g2.fillRect(start, 0, (int) (calculatePixelPerHerz() * (-scopeLower + scopeUpper)), getLineHeight());

            g2.setColor(TUNE);
            g2.drawLine((int) x, 0, (int) x, getLineHeight());

            g2.setColor(TEXT_COLOR);
            if (mouseX != -1 && mouseY != -1 && !mouseDown && mouseY < getLineHeight()) {
                String freq = getDisplayFrequencyAt(mouseX, 100, true);
                g2.drawString(freq, mouseX + 5, mouseY - 5);
            }
        }

    }

    private double calculateDbPerPixel() {
        float diff = fftMax - fftMin;
        return diff / (double) getLineHeight();
    }

    private int calculateHerzPerPixel() {
        return (int) (bandwidth / (double) getWidth());
    }

    private int calculateOffsetAtPoint(int x) {
        return Math.round(x * calculateHerzPerPixel() - bandwidth / 2);
    }

    private double calculatePixelPerDb() {
        float diff = fftMax - fftMin;
        return getLineHeight() / diff;
    }

    private double calculatePixelPerHerz() {
        return getWidth() / (double) bandwidth;
    }

    private int calculateSignalAtPoint(int y) {
        return (int) Math.round(fftMax - y * calculateDbPerPixel());
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

    private void drawSignalLine(Graphics2D g2, int y, Color color) {
        g2.setColor(color);
        g2.drawLine(0, y, getWidth(), y);

        String signal = Integer.toString(calculateSignalAtPoint(y));

        if (y - 5 < getLineHeight()) g2.drawString(signal, 1, y - 5);
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
