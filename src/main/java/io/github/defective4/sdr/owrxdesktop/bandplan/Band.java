package io.github.defective4.sdr.owrxdesktop.bandplan;

import java.awt.Color;

public record Band(int startFreq, int endFreq, Color color, String name) {
}
