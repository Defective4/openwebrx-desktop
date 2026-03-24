package io.github.defective4.sdr.owrxdesktop.application.integration;

import java.util.Arrays;

public enum SearchSort {
    ALPHABETICAL("Alphabetical", 1), DEFAULT("Default", 0), DISTANCE("Distance", 2);

    private final String name;
    private final int priori;

    private SearchSort(String name, int priori) {
        this.name = name;
        this.priori = priori;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    public static SearchSort[] sortedValues() {
        return Arrays.stream(values()).sorted((o1, o2) -> o1.priori - o2.priori).toArray(SearchSort[]::new);
    }

}
