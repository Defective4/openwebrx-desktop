package io.github.defective4.sdr.owrxdesktop.bandplan;

import java.awt.Color;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import io.github.defective4.sdr.owrxdesktop.application.util.ColorEncoder;

public class Bandplan {
    private static final Map<String, String> COLORS = Map.of("hamradio", "#006000", "broadcast", "#000080", "public",
            "#400040", "service", "#800000");
    private final Set<Band> bands = new HashSet<>();
    private final Map<String, Color> colorTags;
    private final Color defaultTagColor;
    private final String name;

    public Bandplan() {
        Map<String, Color> tagged = new HashMap<>();

        for (Entry<String, String> entry : COLORS.entrySet()) {
            tagged.put(entry.getKey(), ColorEncoder.setColorAlpha(Color.decode(entry.getValue()), 200));
        }
        colorTags = Collections.unmodifiableMap(tagged);
        defaultTagColor = colorTags.get("public");
        name = null;
    }

    public Bandplan(Collection<Band> bands, Map<String, Color> colors, String name) {
        colorTags = Map.copyOf(colors);
        defaultTagColor = colors.values().iterator().next();
        this.name = name;
        setBands(bands.stream().collect(Collectors.toUnmodifiableSet()));
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

    public SerializedBandplan serialize() {
        Map<String, String> mappedColors = new HashMap<>();
        colorTags.forEach((s, c) -> mappedColors.put(s, ColorEncoder.toHex(c)));
        return new SerializedBandplan(bands.stream().map(SerializedBand::new).toList(), Map.copyOf(mappedColors), name);
    }

    public void setBands(Set<Band> bands) {
        this.bands.clear();
        this.bands.addAll(bands);
    }
}
