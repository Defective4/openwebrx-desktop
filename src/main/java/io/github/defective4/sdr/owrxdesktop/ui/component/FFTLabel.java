package io.github.defective4.sdr.owrxdesktop.ui.component;

import java.awt.Color;

public record FFTLabel(int freq, String name, Color color, Type type, String mode, String underlying) {
    public static enum Type {
        BOOKMARK, DIAL
    }
}
