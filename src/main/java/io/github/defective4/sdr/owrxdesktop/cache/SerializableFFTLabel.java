package io.github.defective4.sdr.owrxdesktop.cache;

import io.github.defective4.sdr.owrxdesktop.ui.component.FFTLabel;

public record SerializableFFTLabel(int freq, String name, int activeColor, int inactiveColor, FFTLabel.Type type, String mode,
        String underlying) {

}
