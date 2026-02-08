package io.github.defective4.sdr.owrxdesktop.ui.component;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

public class WaterfallPanel extends TuneablePanel implements FFTVisualizer {

    private int fftDecimation = 1;
    private final Deque<BufferedImage> fftLines = new ArrayDeque<>();
    private float fftMax = -20;
    private float fftMin = -88;

    private Color[] theme = new Color[] { Color.black, Color.white };

    public WaterfallPanel() {
        drawScope = false;
    }

    @Override
    public void drawFFT(float[] raw) {
        float[] fft = fftDecimation == 1 ? raw : decimateFFT(raw, fftDecimation);
        BufferedImage image = new BufferedImage(fft.length, 1, BufferedImage.TYPE_INT_RGB);

        for (int i = 0; i < fft.length; i++) {
            float element = fft[i];
            double ratio = calculateFFTValueInRange(element) / calculateFFTRange();
            Color color = theme[theme.length - 1 - (int) Math.max(0, Math.round((theme.length - 1) * ratio))];
            image.setRGB(i, 0, color.getRGB());
        }

        synchronized (fftLines) {
            if (!fftLines.isEmpty() && fftLines.peekFirst().getWidth() != fft.length) fftLines.clear();
            fftLines.addFirst(image);
            while (fftLines.size() > getLineHeight()) {
                fftLines.removeLast();
            }
        }

        repaint();
    }

    public int getFFTDecimation() {
        return fftDecimation;
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
        return getHeight();
    }

    public Color[] getTheme() {
        return theme;
    }

    public void setFFTDecimation(int fftDecimation) {
        if (fftDecimation < 1) throw new IllegalArgumentException("FFT Decimation can't be less than 1");
        this.fftDecimation = fftDecimation;
    }

    @Override
    public void setFFTMax(float min) {
        fftMin = min;
        repaint();
    }

    @Override
    public void setFFTMin(float max) {
        fftMax = max;
        repaint();
    }

    public void setTheme(Color[] theme) {
        if (theme.length < 1) throw new IllegalArgumentException("A theme must contain at least one color");
        this.theme = Objects.requireNonNull(theme);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g2 = (Graphics2D) graphics;
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2.setColor(BG);
        g2.fillRect(0, 0, getWidth(), getHeight());

        int y = 0;
        synchronized (fftLines) {
            for (BufferedImage line : fftLines) {
                g2.drawImage(line, 0, y++, getWidth(), 1, null);
            }
        }

        graphics.setFont(graphics.getFont().deriveFont(12f));
        super.paintComponent(graphics);
    }

    private static float[] decimateFFT(float[] fft, int decimation) {
        float[] decimated = new float[fft.length / decimation];
        for (int i = 0; i < fft.length; i += decimation) {
            int ix = i / decimation;
            if (ix >= decimated.length) continue;
            decimated[ix] = fft[i];
        }
        return decimated;
    }

}
