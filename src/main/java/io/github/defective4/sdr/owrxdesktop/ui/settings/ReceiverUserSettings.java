package io.github.defective4.sdr.owrxdesktop.ui.settings;

import java.util.Objects;
import java.util.Set;

import io.github.defective4.sdr.owrxdesktop.ui.settings.waterfall.BuiltinWaterfallTheme;
import io.github.defective4.sdr.owrxdesktop.ui.settings.waterfall.WaterfallThemeMode;

public class ReceiverUserSettings {
    private BuiltinWaterfallTheme selectedBuiltinWaterfallTheme = BuiltinWaterfallTheme.TURBO;
    private Set<String> waterfallCustomTheme = Set.of("#000000", "#ffffff");
    private WaterfallThemeMode waterfallThemeMode = WaterfallThemeMode.SERVER;

    public BuiltinWaterfallTheme getSelectedBuiltinWaterfallTheme() {
        return selectedBuiltinWaterfallTheme;
    }

    public Set<String> getWaterfallCustomTheme() {
        return waterfallCustomTheme;
    }

    public WaterfallThemeMode getWaterfallThemeMode() {
        return waterfallThemeMode;
    }

    public void setSelectedBuiltinWaterfallTheme(BuiltinWaterfallTheme selectedBuiltinWaterfallTheme) {
        this.selectedBuiltinWaterfallTheme = Objects.requireNonNull(selectedBuiltinWaterfallTheme);
    }

    public void setWaterfallCustomTheme(Set<String> waterfallCustomTheme) {
        this.waterfallCustomTheme = Objects.requireNonNull(waterfallCustomTheme);
    }

    public void setWaterfallThemeMode(WaterfallThemeMode waterfallThemeMode) {
        this.waterfallThemeMode = Objects.requireNonNull(waterfallThemeMode);
    }

}
