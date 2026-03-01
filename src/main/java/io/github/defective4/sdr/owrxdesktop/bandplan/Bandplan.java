package io.github.defective4.sdr.owrxdesktop.bandplan;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class Bandplan {
    private final Set<Band> bands = new HashSet<>();

    public Set<Band> getBands() {
        return Collections.unmodifiableSet(bands);
    }

    public Set<Band> getBandsInRange(int startFreq, int endFreq) {
        return bands.stream().filter(band -> band.endFreq() >= startFreq && band.startFreq() <= endFreq)
                .collect(Collectors.toSet());
    }

    public void setBands(Set<Band> bands) {
        this.bands.clear();
        this.bands.addAll(bands);
    }
}
