package io.github.defective4.sdr.owrxdesktop.ui.component;

import java.awt.Color;

import io.github.defective4.sdr.owrxdesktop.cache.SerializableFFTLabel;

public record FFTLabel(int freq, String name, Color activeColor, Color inactiveColor, Type type, String mode,
        String underlying) {
    public static enum Type {
        CL_BOOKMARK("Personal bookmark"), SRV_BOOKMARK("Server bookmark"), DIAL("Dial frequency");

        private final String name;

        private Type(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

    }

    public SerializableFFTLabel toSerializable() {
        return new SerializableFFTLabel(freq, name, activeColor.getRGB(), inactiveColor.getRGB(), type, mode,
                underlying);
    }
}
