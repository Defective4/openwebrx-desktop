package io.github.defective4.sdr.owrxdesktop.ui.component;

public enum FrequencyUnit {
    G(1000000000, 0), H(1, 3), K(1000, 2), M(1000000, 1);

    private static final String HZ = "Hz";
    private final int multiplier;
    private final int order;

    private FrequencyUnit(int multiplier, int order) {
        this.multiplier = multiplier;
        this.order = order;
    }

    public int getMultiplier() {
        return multiplier;
    }

    public int getOrder() {
        return order;
    }

    @Override
    public String toString() {
        if (this == H) return HZ;
        return name() + HZ;
    }

}
