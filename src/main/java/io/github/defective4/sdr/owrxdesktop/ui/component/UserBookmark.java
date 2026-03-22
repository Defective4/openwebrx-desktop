package io.github.defective4.sdr.owrxdesktop.ui.component;

import java.awt.Color;
import java.util.Optional;

import io.github.defective4.sdr.owrxclient.model.ReceiverMode;

public record UserBookmark(String name, int frequency, ReceiverMode primary, Optional<ReceiverMode> secondary,
        String profile) {

    public FFTLabel toLabel() {
        return new FFTLabel(frequency, name, Color.decode("#00FFFF"), Color.decode("#008383"),
                FFTLabel.Type.CL_BOOKMARK, primary.modulation(), secondary.map(ReceiverMode::modulation).orElse(null));
    }
}
