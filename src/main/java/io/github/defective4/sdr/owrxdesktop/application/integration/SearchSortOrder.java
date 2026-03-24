package io.github.defective4.sdr.owrxdesktop.application.integration;

public enum SearchSortOrder {
    ASC("Ascending"), DESC("Descending");

    private final String name;

    private SearchSortOrder(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }


}
