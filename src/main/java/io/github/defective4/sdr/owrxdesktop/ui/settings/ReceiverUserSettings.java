package io.github.defective4.sdr.owrxdesktop.ui.settings;

import java.awt.Color;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
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

    private boolean dynamicColorMixing = true;
    private boolean enableFreeTuning;

    private String magicKey;
    private BuiltinWaterfallTheme selectedBuiltinWaterfallTheme = BuiltinWaterfallTheme.TURBO;
    private List<String> waterfallCustomTheme = List.of("#000000", "#ffffff");

    private WaterfallThemeMode waterfallThemeMode = WaterfallThemeMode.SERVER;

    public ReceiverUserSettings() {

    }

    @Override
    public ReceiverUserSettings clone() {
        ReceiverUserSettings settings = new ReceiverUserSettings();
        for (Field field : getClass().getDeclaredFields()) if (!Modifier.isFinal(field.getModifiers())) {
            Object val;
            try {
                val = field.get(this);
                field.set(settings, val);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }
        return settings;
    }

    public String getMagicKey() {
        return magicKey == null ? "" : magicKey;
    }

    public BuiltinWaterfallTheme getSelectedBuiltinWaterfallTheme() {
        return selectedBuiltinWaterfallTheme;
    }

    public List<String> getWaterfallCustomTheme() {
        return Collections.unmodifiableList(waterfallCustomTheme);
    }

    public WaterfallThemeMode getWaterfallThemeMode() {
        return waterfallThemeMode;
    }

    public boolean isDynamicColorMixing() {
        return dynamicColorMixing;
    }

    public boolean isEnableFreeTuning() {
        return enableFreeTuning;
    }

    public void setDynamicColorMixing(boolean dynamicColorMixing) {
        this.dynamicColorMixing = dynamicColorMixing;
    }

    public void setEnableFreeTuning(boolean enableFreeTuning) {
        this.enableFreeTuning = enableFreeTuning;
    }

    public void setMagicKey(String magicKey) {
        this.magicKey = magicKey;
    }

    public void setSelectedBuiltinWaterfallTheme(BuiltinWaterfallTheme selectedBuiltinWaterfallTheme) {
        this.selectedBuiltinWaterfallTheme = Objects.requireNonNull(selectedBuiltinWaterfallTheme);
    }

    public void setWaterfallCustomTheme(List<String> waterfallCustomTheme) {
        this.waterfallCustomTheme = Objects.requireNonNull(List.copyOf(waterfallCustomTheme));
    }

    public void setWaterfallThemeMode(WaterfallThemeMode waterfallThemeMode) {
        this.waterfallThemeMode = Objects.requireNonNull(waterfallThemeMode);
    }

    public static Optional<Color[]> getTheme(String index) {
        return Optional.ofNullable(themeIndex.get(index));
    }
}
