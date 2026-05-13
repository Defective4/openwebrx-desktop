package io.github.defective4.sdr.owrxdesktop.audio;

public enum RecorderQuality {
    AUTO("Auto (Recommended)", false), k12("12 KHz", false), k48("48 KHz", true);

    private final boolean high;
    private final String name;

    private RecorderQuality(String name, boolean b) {
        this.name = name;
        high = b;
    }

    public boolean isHigh() {
        return high;
    }

    @Override
    public String toString() {
        return name;
    }

}
