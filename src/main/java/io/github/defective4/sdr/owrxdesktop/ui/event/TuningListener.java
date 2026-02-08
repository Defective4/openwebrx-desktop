package io.github.defective4.sdr.owrxdesktop.ui.event;

public interface TuningListener {
    void tuned(int offset);

    void zoomChanged(int x, int width);
}
