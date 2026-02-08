package io.github.defective4.sdr.owrxdesktop.ui.component;

import static java.awt.RenderingHints.*;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class FFTPanel extends TuneablePanel implements FFTVisualizer {
    private static final Color FFT_COLOR = Color.white;
    private static final Color FREQ_BAR = Color.decode("#282525");
    private static final Color LINE = Color.decode("#3F3B3B");
    private static final Color LINE_CENTER = Color.white;

    private float[] fft = new float[0];

    private final Object fftLock = new Object();

    private float fftMax = -20;
    private float fftMin = -88;

    public FFTPanel() {

    }

    @Override
    public void drawFFT(float[] fft) {
        synchronized (fftLock) {
            this.fft = fft;
        }
        repaint();
    }

    @Override
    public float getFFTMax() {
        return fftMax;
    }

    @Override
    public float getFFTMin() {
        return fftMin;
    }

    @Override
    public int getLineHeight() {
        return getHeight() - 24;
    }

    @Override
    public void setFFTMax(float fftMax) {
        this.fftMax = fftMax;
    }

    @Override
    public void setFFTMin(float fftMin) {
        this.fftMin = fftMin;
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

        double dy = 0;
        double dbStep = calculatePixelPerDb() * 10d;

        dy += dbStep;
        while (dy < getLineHeight()) {
            drawSignalLine(g2, (int) Math.round(dy), LINE);
            dy += dbStep;
        }

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

        g2.setColor(FFT_COLOR);

        synchronized (fftLock) {
            int prevX = 0;
            float prevVal = -1;
            if (fft.length > 0) for (int i = 0; i < fft.length; i++) {
                int ix = (int) Math.round(i / (double) fft.length * getWidth());
                float range = calculateFFTRange();
                float valueInRange = calculateFFTValueInRange(fft[i]);
                double r = valueInRange / range;

                int y = (int) (getLineHeight() * r);

                if (prevVal != -1) {
                    g2.drawLine(prevX, (int) prevVal, ix, y);
                }
                prevVal = y;
                prevX = ix;
            }
        }

        super.paintComponent(graphics);
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

    private static int calculateDrawingStep(double pxPerHz) {
        int part = 3 - (int) Math.floor(pxPerHz * 1e4f / 2d);
        return (int) (Math.max(part, 1) * 100e3f);
    }
}
