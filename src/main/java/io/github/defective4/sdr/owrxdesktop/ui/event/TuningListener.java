package io.github.defective4.sdr.owrxdesktop.ui.event;

public interface TuningListener {
    void scopeChanged(int scopeLower, int scopeUpper);

    void tuned(int offset);

    void zoomChanged(int x, int width);
}
