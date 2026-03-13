package io.github.defective4.sdr.owrxdesktop.ui.component;

import java.util.Set;

import io.github.defective4.sdr.owrxdesktop.bandplan.Band;
import io.github.defective4.sdr.owrxdesktop.bandplan.Bandplan;

public abstract class BandplanPanel extends TuneablePanel {

    private final Bandplan bandplan;
    private Set<Band> visibleBands = Set.of();

    public BandplanPanel(Bandplan bandplan) {
        this.bandplan = bandplan;
    }

    public Bandplan getBandplan() {
        return bandplan;
    }

    public Set<Band> getVisibleBands() {
        return visibleBands;
    }

    @Override
    public void setBandwidth(int bandwidth) {
        super.setBandwidth(bandwidth);
        updateVisibleBands();
    }

    @Override
    public void setCenterFrequency(int centerFrequency) {
        super.setCenterFrequency(centerFrequency);
        updateVisibleBands();
    }

    @Override
    public void tune(int offset, boolean fireEvents, boolean snap) {
        super.tune(offset, fireEvents, snap);
        updateVisibleBands();
    }

    public void updateVisibleBands() {
        int lo = centerFrequency - bandwidth / 2;
        int hi = centerFrequency + bandwidth / 2;
        visibleBands = bandplan.getBandsInRange(lo, hi);
    }

}
