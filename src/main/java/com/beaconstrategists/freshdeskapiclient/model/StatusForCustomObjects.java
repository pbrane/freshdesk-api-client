package com.beaconstrategists.freshdeskapiclient.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum StatusForCustomObjects {
    Open("Open"),
    Pending("Pending"),
    Resolved("Resolved"),
    Closed("Closed");

    private final String value;

    StatusForCustomObjects(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
