package io.github.defective4.sdr.owrxdesktop.ui.component;

import static java.awt.Cursor.*;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import io.github.defective4.sdr.owrxdesktop.bandplan.Band;
import io.github.defective4.sdr.owrxdesktop.bandplan.Bandplan;
import io.github.defective4.sdr.owrxdesktop.ui.component.FFTLabel.Type;

public class FFTPanel extends BandplanPanel {
    public static interface FFTPanelListener {
        void labelClicked(FFTLabel label);
    }

    private static record LabelSpace(Rectangle rect, FFTLabel label) {
    }

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

    private final Set<FFTLabel.Type> labelRenderMode = new HashSet<>(Set.of(FFTLabel.Type.values()));

    private final List<FFTLabel> labels = new ArrayList<>();
    private final Deque<LabelSpace> occupied = new LinkedList<>();

    private final List<FFTPanelListener> panelListeners = new CopyOnWriteArrayList<>();

    private FFTLabel selectedLabel;

    private boolean showBandplan = true;
    private boolean solid;

    public FFTPanel(Bandplan bandplan) {
        super(bandplan);
        MouseAdapter adapter = new MouseAdapter() {
            Cursor def = new Cursor(DEFAULT_CURSOR);
            Cursor pointer = new Cursor(HAND_CURSOR);

            @Override
            public void mouseMoved(MouseEvent e) {
                selectedLabel = getLabelAt(e.getX(), e.getY());
                if (selectedLabel != null) {
                    setDrawFrequencyLabel(false);
                    setCursor(pointer);
                } else {
                    setDrawFrequencyLabel(true);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                FFTLabel label = getLabelAt(e.getX(), e.getY());
                if (label != null) {
                    panelListeners.forEach(ls -> ls.labelClicked(label));
                }
            }
        };
        addMouseMotionListener(adapter);
        addMouseListener(adapter);
    }

    public boolean addPanelListener(FFTPanelListener listener) {
        return panelListeners.add(Objects.requireNonNull(listener));
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

    public List<FFTPanelListener> getPanelListeners() {
        return Collections.unmodifiableList(panelListeners);
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

    public boolean removePanelListener(FFTPanelListener listener) {
        return panelListeners.remove(listener);
    }

    public void resetLabels() {
        labels.clear();
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
        if (render) {
            if (!labelRenderMode.contains(type)) {
                labelRenderMode.add(type);
            }
        } else
            labelRenderMode.remove(type);
    }

    public void setLabels(Collection<FFTLabel> labels) {
        List<Type> types = labels.stream().map(FFTLabel::type).toList();
        synchronized (this.labels) {
            this.labels.stream().filter(label -> types.contains(label.type())).toList().forEach(this.labels::remove);
            this.labels.addAll(labels);
        }
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

        synchronized (occupied) {
            occupied.clear();
        }

        synchronized (labels) {
            for (FFTLabel label : labels) {
                if (!labelRenderMode.contains(label.type()) || label.freq() < centerFrequency - bandwidth / 2
                        || label.freq() > centerFrequency + bandwidth / 2)
                    continue;
                int offset = (int) Math.floor((label.freq() - centerFrequency) * pxPerHz) + center;
                int width = metrics.stringWidth(label.name());

                int y = labelHeight;

                int from = offset - width / 2;
                int to = offset + width / 2;

                synchronized (occupied) {
                    for (LabelSpace lab : occupied) {
                        Rectangle rect = lab.rect;
                        if (from < rect.x + rect.width + 16 && to > rect.x - 16) y += labelHeight * 1.5;
                        if (y > getLineHeight()) {
                            y -= labelHeight * 1.5;
                            break;
                        }
                    }

                    if (y > getLineHeight()) continue;

                    g2.setColor(label == selectedLabel || label.freq() == centerFrequency + super.offset
                            ? label.activeColor()
                            : label.inactiveColor());
                    g2.drawLine(offset, y, offset, getLineHeight());
                    g2.drawLine(from, y, to, y);

                    int ty = y - labelHeight / 8;

                    g2.drawString(label.name(), from, ty);
                    occupied.addFirst(
                            new LabelSpace(new Rectangle(from, ty - labelHeight / 2, to - from, labelHeight), label));
                }
            }
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

    private FFTLabel getLabelAt(int x, int y) {
        synchronized (occupied) {
            for (LabelSpace entry : occupied) {
                Rectangle rect = entry.rect;
                if (x > rect.x && x < rect.x + rect.width && y > rect.y && y < rect.y + rect.height) {
                    return entry.label;
                }
            }
        }
        return null;
    }

    private static int calculateDrawingStep(double pxPerHz) {
        int part = 3 - (int) Math.floor(pxPerHz * 1e4f / 2d);
        return (int) (Math.max(part, 1) * 100e3f);
    }
}
