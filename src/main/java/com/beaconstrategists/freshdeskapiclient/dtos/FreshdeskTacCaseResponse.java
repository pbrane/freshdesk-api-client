package com.beaconstrategists.freshdeskapiclient.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FreshdeskTacCaseResponse {

    @JsonProperty("display_id")
    private String displayId;

    @JsonProperty("created_time")
    private Long createdTime;

    @JsonProperty("updated_time")
    private Long updatedTime;

    @JsonProperty("data")
    private FreshdeskTacCaseDto data;

    @JsonProperty("version")
    private int version;
}
