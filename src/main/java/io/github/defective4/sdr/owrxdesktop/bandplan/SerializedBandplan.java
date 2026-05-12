package io.github.defective4.sdr.owrxdesktop.bandplan;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record SerializedBandplan(List<SerializedBand> list, Map<String, String> colors, String name) {
    public Bandplan deserialize() {
        Map<String, Color> colors = new HashMap<>();
        this.colors.forEach((s, c) -> colors.put(s, Color.decode(c)));
        return new Bandplan(list.stream().map(SerializedBand::deserialize).toList(), colors, name);
    }
}
