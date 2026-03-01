package io.github.defective4.sdr.owrxdesktop.ui.component;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

public class WaterfallPanel extends TuneablePanel {

    private boolean colorMixing = true;
    private final Deque<BufferedImage> fftLines = new ArrayDeque<>();
    private float fftMax = -20;

    private float fftMin = -88;

    private Color[] theme = new Color[] { Color.black, Color.white };

    public WaterfallPanel() {
        drawScope = true;
    }

    @Override
    public void drawFFT(float[] fft, int offset) {
        int fftLength = fft.length - offset;
        BufferedImage image = new BufferedImage(fftLength, 1, BufferedImage.TYPE_INT_RGB);

        for (int i = 0; i < fftLength; i++) {
            float element = fft[i + offset];
            double ratio = calculateFFTValueInRange(element) / calculateFFTRange();
            if (ratio > 1) ratio = 1;
            if (ratio < 0) ratio = 0;
            Color color;
            if (colorMixing) {
                double index = (theme.length - 1) * ratio;

                Color upper = theme[(int) Math.ceil(index)];
                Color lower = theme[(int) Math.floor(index)];

                double gradientRatio = index - Math.floor(index);

                int r = gradient(upper.getRed(), lower.getRed(), gradientRatio);
                int g = gradient(upper.getGreen(), lower.getGreen(), gradientRatio);
                int b = gradient(upper.getBlue(), lower.getBlue(), gradientRatio);

                color = new Color(r, g, b);
            } else {
                int index = (int) Math.round((theme.length - 1) * ratio);
                color = theme[index];
            }
            image.setRGB(i, 0, color.getRGB());
        }

        synchronized (fftLines) {
            if (!fftLines.isEmpty() && fftLines.peekFirst().getWidth() != fftLength) fftLines.clear();
            fftLines.addFirst(image);
            while (fftLines.size() > getLineHeight()) {
                fftLines.removeLast();
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
        return getHeight();
    }

    public Color[] getTheme() {
        return theme;
    }

    public boolean hasColorMixing() {
        return colorMixing;
    }

    public void setColorMixing(boolean colorMixing) {
        this.colorMixing = colorMixing;
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

    private static int gradient(int a, int b, double ratio) {
        int diff = Math.max(a, b) - Math.min(a, b);
        return (int) Math.round(Math.min(a, b) + diff * ratio);
    }

}
