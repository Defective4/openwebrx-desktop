package io.github.defective4.sdr.owrxdesktop.ui.component;

public interface FFTVisualizer {
    public default double calculateDbPerPixel() {
        float diff = getFFTMax() - getFFTMin();
        return diff / (double) getLineHeight();
    }

    public default float calculateFFTRange() {
        float range = getFFTMax() - getFFTMin();
        return range;
    }

    public default float calculateFFTValueInRange(float element) {
        return getFFTMax() - element;
    }

    public default double calculatePixelPerDb() {
        float diff = getFFTMax() - getFFTMin();
        return getLineHeight() / diff;
    }

    public default int calculateSignalAtPoint(int y) {
        return (int) Math.round(getFFTMax() - y * calculateDbPerPixel());
    }

    void drawFFT(float[] fft);

    float getFFTMax();

    float getFFTMin();

    int getLineHeight();

    void setFFTMax(float min);

    void setFFTMin(float max);
}
