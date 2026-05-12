package io.github.defective4.sdr.owrxdesktop.bandplan;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.defective4.sdr.owrxdesktop.application.util.ColorEncoder;

public record SerializedBandplan(List<SerializedBand> list, Map<String, String> colors, String name) {
    public Bandplan deserialize() {
        Map<String, Color> colors = new HashMap<>();
        this.colors.forEach((s, c) -> {
            Color color = Color.decode(c);
            colors.put(s, ColorEncoder.setColorAlpha(color, 100));
        });
        return new Bandplan(list.stream().map(SerializedBand::deserialize).toList(), colors, name);
    }
}
