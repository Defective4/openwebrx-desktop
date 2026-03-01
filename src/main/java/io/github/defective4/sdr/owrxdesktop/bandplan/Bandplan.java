package io.github.defective4.sdr.owrxdesktop.bandplan;

import java.util.Set;
import java.util.stream.Collectors;

public record Bandplan(Set<Band> bands) {
    public Set<Band> getBandsInRange(int startFreq, int endFreq) {
        return bands.stream().filter(band -> band.endFreq() >= startFreq && band.startFreq() <= endFreq)
                .collect(Collectors.toSet());
    }
}
