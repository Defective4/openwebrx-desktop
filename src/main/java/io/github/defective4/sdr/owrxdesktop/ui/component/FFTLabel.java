package io.github.defective4.sdr.owrxdesktop.ui.component;

import java.awt.Color;

public record FFTLabel(int freq, String name, Color color, Type type) {
    public static enum Type {
        DIAL
    }
}
