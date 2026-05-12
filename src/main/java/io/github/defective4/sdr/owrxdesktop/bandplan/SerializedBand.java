package io.github.defective4.sdr.owrxdesktop.bandplan;

import java.awt.Color;

import io.github.defective4.sdr.owrxdesktop.application.util.ColorEncoder;

public record SerializedBand(int startFreq, int endFreq, String hex, String name) {
    public SerializedBand(Band band) {
        this(band.startFreq(), band.endFreq(), ColorEncoder.toHex(band.color()), band.name());
    }

    public Band deserialize() {
        return new Band(startFreq, endFreq, Color.decode(hex), name);
    }
}
