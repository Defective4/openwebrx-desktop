package io.github.defective4.sdr.owrxdesktop.ui.event;

public abstract class TuningAdapter implements TuningListener {

    @Override
    public void tuned(int offset) {}

    @Override
    public void zoomChanged(int x, int width) {}

}
