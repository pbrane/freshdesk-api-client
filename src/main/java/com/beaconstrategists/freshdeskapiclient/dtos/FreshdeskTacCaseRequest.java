package com.beaconstrategists.freshdeskapiclient.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FreshdeskTacCaseRequest {

    @JsonProperty("data")
    private FreshdeskTacCaseDto data;

    public FreshdeskTacCaseRequest(FreshdeskTacCaseDto data) {
        this.data = data;
    }
}
