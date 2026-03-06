package io.github.defective4.sdr.owrxdesktop.ui.component;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.github.defective4.sdr.owrxdesktop.bandplan.Band;
import io.github.defective4.sdr.owrxdesktop.bandplan.Bandplan;

public class FFTPanel extends BandplanPanel {
    private static final Color FFT_COLOR = Color.white;
    private static final Color FFT_MAX_COLOR = Color.yellow;
    private static final Color FREQ_BAR = Color.decode("#282525");
    private static final Color LINE = Color.decode("#3F3B3B");
    private static final Color LINE_CENTER = Color.white;
    private boolean drawMaxValues;
    private float[] fft = new float[0];

    private final Object fftLock = new Object();

    private float fftMax = -20;

    private float fftMin = -88;

    private int fftOffset;
    private float[] fftValuesMax = new float[0];
    private final Object fftValuesMaxLock = new Object();
    private final Map<FFTLabel.Type, Boolean> labelRenderMode = new HashMap<>();
    private final List<FFTLabel> labels = new ArrayList<>();

    private boolean showBandplan = true;

    private boolean solid;

    public FFTPanel(Bandplan bandplan) {
        super(bandplan);
    }

    public void addLabel(FFTLabel label) {
        labels.removeAll(labels.stream().filter(l -> l.freq() == label.freq()).toList());
        labels.add(label);
    }

    @Override
    public void drawFFT(float[] fft, int offset) {
        fftOffset = offset;
        synchronized (fftLock) {
            this.fft = fft;
        }
        synchronized (fftValuesMaxLock) {
            if (fftValuesMax.length != fft.length) {
                fftValuesMax = new float[fft.length];
                System.arraycopy(fft, 0, fftValuesMax, 0, fft.length);
            }
            for (int i = 0; i < fftValuesMax.length; i++) {
                fftValuesMax[i] = Math.max(fftValuesMax[i], fft[i]);
            }
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

    public boolean isDrawMaxValues() {
        return drawMaxValues;
    }

    public boolean isShowBandplan() {
        return showBandplan;
    }

    public boolean isSolid() {
        return solid;
    }

    public void resetMaxFFT() {
        synchronized (fftValuesMaxLock) {
            fftValuesMax = new float[0];
        }
        repaint();
    }

    public void setDrawMaxValues(boolean drawMaxValues) {
        this.drawMaxValues = drawMaxValues;
        if (!drawMaxValues) {
            fftValuesMax = new float[0];
        }
    }

    @Override
    public void setFFTMax(float fftMax) {
        this.fftMax = fftMax;
    }

    @Override
    public void setFFTMin(float fftMin) {
        this.fftMin = fftMin;
    }

    public void setLabelRender(FFTLabel.Type type, boolean render) {
        labelRenderMode.put(type, render);
    }

    public void setShowBandplan(boolean showBandplan) {
        this.showBandplan = showBandplan;
        repaint();
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
            float prevMax = -1;

            g2.setColor(FFT_COLOR);

            int fftLength = fft.length - fftOffset;
            if (fftLength > 0) for (int i = 0; i < fftLength; i++) {
                int ix = (int) Math.round(i / (double) fftLength * getWidth());
                float range = calculateFFTRange();
                float valueInRange = calculateFFTValueInRange(fft[i + fftOffset]);
                double r = valueInRange / range;

                int y = (int) (getLineHeight() * r);

                if (drawMaxValues && fft.length == fftValuesMax.length) {
                    synchronized (fftValuesMaxLock) {
                        float maxValue = calculateFFTValueInRange(fftValuesMax[i + fftOffset]);
                        int maxY = (int) (getLineHeight() * (maxValue / range));

                        g2.setColor(FFT_MAX_COLOR);
                        if (maxValue != -1) {
                            g2.drawLine(prevX, (int) prevMax, ix, maxY);
                        }
                        g2.setColor(FFT_COLOR);
                        prevMax = maxY;
                    }
                }

                if (solid) {
                    g2.drawLine(ix, getLineHeight(), ix, y);
                } else {
                    if (prevVal != -1) {
                        g2.drawLine(prevX, (int) prevVal, ix, y);
                    }
                    prevVal = y;
                }
                prevX = ix;
            }
        }

        g2.setColor(LINE);
        g2.fillRect(0, getLineHeight() + 1, getWidth(), getHeight());

        drawFrequencyLabel(g2, center);

        x = center;
        while (x < getWidth()) {
            x += pxPerHz * step;
            drawFrequencyLabel(g2, (int) x);
        }

        x = center;
        while (x > 0) {
            x -= pxPerHz * step;
            drawFrequencyLabel(g2, (int) x);
        }

        FontMetrics metrics = g2.getFontMetrics();
        int labelHeight = metrics.getHeight();

        Set<Rectangle> occupied = new HashSet<>();

        for (FFTLabel label : labels) {
            if (!labelRenderMode.getOrDefault(label.type(), true) || label.freq() < centerFrequency - bandwidth / 2 || label.freq() > centerFrequency + bandwidth / 2)
                continue;
            int offset = (int) Math.floor((label.freq() - centerFrequency) * pxPerHz) + center;
            int width = metrics.stringWidth(label.name());

            int y = labelHeight;

            int from = offset - width / 2;
            int to = offset + width / 2;

            for (Rectangle rect : occupied) {
                if (from < rect.x + rect.width + 16 && to > rect.x - 16) y += labelHeight * 1.5;
            }

            while (y > getLineHeight() && y > labelHeight) y -= labelHeight * 1.5;
            if (y > getLineHeight()) continue;

            g2.setColor(label.color());
            g2.drawLine(offset, y, offset, getLineHeight());
            g2.drawLine(from, y, to, y);

            g2.drawString(label.name(), from, y - y / 8);
            occupied.add(new Rectangle(from, 0, to - from, 0));
        }

        if (showBandplan) {
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
        }

        super.paintComponent(graphics);
    }

    private void drawFrequencyLabel(Graphics2D g2, int x) {
        g2.setColor(TEXT_COLOR);
        String displayFreq = getDisplayFrequencyAt(x, 10, false);

        FontMetrics metrics = g2.getFontMetrics();
        int width = metrics.stringWidth(displayFreq);
        int textX = x - width / 2;

        if (textX > 0 && textX + width < getWidth())
            g2.drawString(displayFreq, textX, getLineHeight() + metrics.getHeight());
    }

    private void drawFrequencyLine(Graphics2D g2, int x, Color color) {
        g2.setColor(color);
        g2.drawLine(x, 0, x, getLineHeight());

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
