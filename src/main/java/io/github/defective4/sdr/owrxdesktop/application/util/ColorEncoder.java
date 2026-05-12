package io.github.defective4.sdr.owrxdesktop.application.util;

import java.awt.Color;

public class ColorEncoder {
    private ColorEncoder() {}

    public static Color setColorAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    public static String toHex(Color color) {
        return String.format("#%s%s%s", formatHex(color.getRed()), formatHex(color.getGreen()),
                formatHex(color.getBlue()));
    }

    private static String formatHex(int i) {
        String hex = Integer.toHexString(i);
        if (hex.length() < 2) hex = "0" + hex;
        return hex;
    }
}
