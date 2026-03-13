package io.github.defective4.sdr.owrxdesktop.ui.settings;

import java.awt.Color;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.github.defective4.sdr.owrxdesktop.ui.settings.waterfall.BuiltinWaterfallTheme;
import io.github.defective4.sdr.owrxdesktop.ui.settings.waterfall.WaterfallThemeMode;

public class ReceiverUserSettings {

    private static final Map<String, Color[]> themeIndex = new HashMap<>();

    static {
        try (Reader reader = new InputStreamReader(ReceiverUserSettings.class.getResourceAsStream("/themes.json"))) {
            JsonObject obj = JsonParser.parseReader(reader).getAsJsonObject();
            for (Entry<String, JsonElement> entry : obj.entrySet()) {
                if (entry.getValue() instanceof JsonArray array) {
                    String key = entry.getKey();
                    Color[] colors = array.asList().stream().map(el -> Color.decode(el.getAsString()))
                            .toArray(Color[]::new);
                    themeIndex.put(key, colors);
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private BuiltinWaterfallTheme selectedBuiltinWaterfallTheme = BuiltinWaterfallTheme.TURBO;
    private List<String> waterfallCustomTheme = List.of("#000000", "#ffffff");

    private WaterfallThemeMode waterfallThemeMode = WaterfallThemeMode.SERVER;

    public ReceiverUserSettings() {

    }

    public ReceiverUserSettings(BuiltinWaterfallTheme selectedBuiltinWaterfallTheme, List<String> waterfallCustomTheme,
            WaterfallThemeMode waterfallThemeMode) {
        super();
        this.selectedBuiltinWaterfallTheme = selectedBuiltinWaterfallTheme;
        this.waterfallCustomTheme = waterfallCustomTheme;
        this.waterfallThemeMode = waterfallThemeMode;
    }

    public BuiltinWaterfallTheme getSelectedBuiltinWaterfallTheme() {
        return selectedBuiltinWaterfallTheme;
    }

    public List<String> getWaterfallCustomTheme() {
        return waterfallCustomTheme;
    }

    public WaterfallThemeMode getWaterfallThemeMode() {
        return waterfallThemeMode;
    }

    public void setSelectedBuiltinWaterfallTheme(BuiltinWaterfallTheme selectedBuiltinWaterfallTheme) {
        this.selectedBuiltinWaterfallTheme = Objects.requireNonNull(selectedBuiltinWaterfallTheme);
    }

    public void setWaterfallCustomTheme(List<String> waterfallCustomTheme) {
        this.waterfallCustomTheme = Objects.requireNonNull(waterfallCustomTheme);
    }

    public void setWaterfallThemeMode(WaterfallThemeMode waterfallThemeMode) {
        this.waterfallThemeMode = Objects.requireNonNull(waterfallThemeMode);
    }

    public static Optional<Color[]> getTheme(String index) {
        return Optional.ofNullable(themeIndex.get(index));
    }

}
