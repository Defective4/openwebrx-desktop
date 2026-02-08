package io.github.defective4.sdr.owrxdesktop.ui.component;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

public class WaterfallPanel extends TuneablePanel implements FFTVisualizer {

    private int fftDecimation = 1;
    private float fftMax = -20;
    private float fftMin = -88;
    private final Deque<float[]> fftQueue = new ArrayDeque<>();

    private BufferedImage lineBuffer;

    private final Color[] theme;

    public WaterfallPanel(Color... theme) {
        this.theme = Objects.requireNonNull(theme);
    }

    @Override
    public void drawFFT(float[] fft) {
        synchronized (fftQueue) {
            if (!fftQueue.isEmpty()) {
                if (fft.length != fftQueue.element().length) fftQueue.clear();
            }
            fftQueue.addFirst(fft);
            if (fftQueue.size() > getLineHeight() / 2) {
                fftQueue.removeLast();
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

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g2 = (Graphics2D) graphics;
        g2.setColor(BG);
        g2.fillRect(0, 0, getWidth(), getHeight());
        synchronized (fftQueue) {
            if (fftQueue.isEmpty()) return;
            int fftSize = fftQueue.element().length;
            int decimation = (int) Math.max(1, Math.floor(fftSize / (double) getWidth())) + fftDecimation - 1;
            fftSize = fftSize / decimation;
            double pixelSpacing = getWidth() / (double) fftSize;
            int pixelWidth = (int) Math.max(Math.ceil(pixelSpacing), 1);
            int lineWidth = (int) Math.ceil(pixelWidth * (double) fftSize);
            if (lineBuffer == null || lineBuffer.getWidth() != lineWidth)
                lineBuffer = new BufferedImage(lineWidth, 1, BufferedImage.TYPE_INT_RGB);
            int y = 0;
            for (float[] rawFFT : fftQueue) {
                float[] fft = decimation > 1 ? decimateFFT(rawFFT, decimation) : rawFFT;
                for (int i = 0; i < fft.length; i++) {
                    float fftElement = fft[i];
                    float range = calculateFFTRange();
                    float valInRange = calculateFFTValueInRange(fftElement);
                    double ratio = valInRange / range;
                    Color color = theme[theme.length - 1 - (int) Math.round(Math.max((theme.length - 1) * ratio, 0))];

                    int x = (int) Math.round(i * pixelSpacing);
                    for (int j = 0; j < pixelWidth; j++) {
                        lineBuffer.setRGB(x + j, 0, color.getRGB());
                    }
                }
                g2.drawImage(lineBuffer, 0, y += 2, lineWidth, 2, null);
            }
        }
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
