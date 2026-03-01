package io.github.defective4.sdr.owrxdesktop.ui.component;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Set;

import io.github.defective4.sdr.owrxdesktop.bandplan.Band;
import io.github.defective4.sdr.owrxdesktop.bandplan.Bandplan;

public class FFTPanel extends BandplanPanel {
    private static final Color FFT_COLOR = Color.white;
    private static final Color FREQ_BAR = Color.decode("#282525");
    private static final Color LINE = Color.decode("#3F3B3B");
    private static final Color LINE_CENTER = Color.white;
    private float[] fft = new float[0];

    private final Object fftLock = new Object();

    private float fftMax = -20;

    private float fftMin = -88;

    private int fftOffset;
    private boolean solid;

    public FFTPanel(Bandplan bandplan) {
        super(bandplan);
    }

    @Override
    public void drawFFT(float[] fft, int offset) {
        fftOffset = offset;
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

    public boolean isSolid() {
        return solid;
    }

    @Override
    public void setFFTMax(float fftMax) {
        this.fftMax = fftMax;
    }

    @Override
    public void setFFTMin(float fftMin) {
        this.fftMin = fftMin;
    }

    public void setSolid(boolean solid) {
        this.solid = solid;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g2 = (Graphics2D) graphics;
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2.setColor(BG);
        g2.fillRect(0, 0, getWidth(), getHeight());
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
            int fftLength = fft.length - fftOffset;
            if (fftLength > 0) for (int i = 0; i < fftLength; i++) {
                int ix = (int) Math.round(i / (double) fftLength * getWidth());
                float range = calculateFFTRange();
                float valueInRange = calculateFFTValueInRange(fft[i + fftOffset]);
                double r = valueInRange / range;

                int y = (int) (getLineHeight() * r);

                if (solid) {
                    g2.drawLine(ix, getLineHeight(), ix, y);
                } else {
                    if (prevVal != -1) {
                        g2.drawLine(prevX, (int) prevVal, ix, y);
                    }
                    prevVal = y;
                    prevX = ix;
                }
            }
        }

        Set<Band> bands = getVisibleBands();
        if (!bands.isEmpty()) {
            int low = centerFrequency - bandwidth / 2;
            int hi = centerFrequency + bandwidth / 2;
            for (Band band : bands) {
                int loDiff = Math.max(0, band.startFreq() - low);
                int hiDiff = Math.max(0, hi - band.endFreq());
                int startX = (int) Math.round(loDiff * pxPerHz);
                int width = (int) (getWidth() - Math.round(hiDiff * pxPerHz) - startX);
                g2.setColor(band.color());
                g2.fillRect(startX, getHeight() - 23 - 16, width, 16);

                String str = band.name();

                FontMetrics metrics = g2.getFontMetrics();
                int strWidth = metrics.stringWidth(str);
                if (strWidth > width) {
                    str = "...";
                    strWidth = metrics.stringWidth(str);
                }

                if (strWidth > width) continue;

                int strX = startX + width / 2 - strWidth / 2;

                g2.setColor(Color.white);
                g2.drawString(str, strX, getHeight() - 23 - 4);
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
