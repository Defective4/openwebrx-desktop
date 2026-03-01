package io.github.defective4.sdr.owrxdesktop.bandplan;

import java.awt.Color;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class Bandplan {
    private final Set<Band> bands = new HashSet<>();
    private final Map<String, Color> colorTags;
    private final Color defaultTagColor;

    public Bandplan() {
        Map<String, String> colors = Map.of("hamradio", "#006000", "broadcast", "#000080", "public", "#400040",
                "service", "#800000"); // TODO make it modifiable

        Map<String, Color> tagged = new HashMap<>();

        for (Entry<String, String> entry : colors.entrySet()) {
            Color decoded = Color.decode(entry.getValue());
            Color corrected = new Color(decoded.getRed(), decoded.getGreen(), decoded.getBlue(), 200);
            tagged.put(entry.getKey(), corrected);
        }
        colorTags = Collections.unmodifiableMap(tagged);
        defaultTagColor = colorTags.get("public");
    }

    public Set<Band> getBands() {
        return Collections.unmodifiableSet(bands);
    }

    public Set<Band> getBandsInRange(float startFreq, float endFreq) {
        return bands.stream().filter(band -> band.endFreq() >= startFreq && band.startFreq() <= endFreq)
                .collect(Collectors.toSet());
    }

    public Optional<Color> getColorForTag(String tag) {
        return Optional.ofNullable(colorTags.get(tag.toLowerCase()));
    }

    public Color getDefaultTagColor() {
        return defaultTagColor;
    }

    public void setBands(Set<Band> bands) {
        this.bands.clear();
        this.bands.addAll(bands);
    }
}
