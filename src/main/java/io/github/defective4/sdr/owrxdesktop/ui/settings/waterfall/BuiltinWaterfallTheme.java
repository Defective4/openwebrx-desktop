package io.github.defective4.sdr.owrxdesktop.ui.settings.waterfall;

public enum BuiltinWaterfallTheme {
    LEGACY("Legacy by Andras (HA7ILM)", "legacy"), TURBO("Google Turbo", "turbo");

    private final String name;
    private final String reference;

    private BuiltinWaterfallTheme(String name, String reference) {
        this.name = name;
        this.reference = reference;
    }

    public String getReference() {
        return reference;
    }

    @Override
    public String toString() {
        return name;
    }

}
