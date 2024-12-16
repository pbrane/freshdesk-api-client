package com.beaconstrategists.freshdeskapiclient.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data //fixme: check this lombok configuration
public class FreshdeskTacCaseResponse {

    //fixme: do we really need this property annotations?
    @JsonProperty("display_id")
    private String displayId;

    @JsonProperty("created_time")
    private Long createdTime;

    @JsonProperty("updated_time")
    private Long updatedTime;

    @JsonProperty("data")
    private FreshdeskTacCaseResponseDto data;

    @JsonProperty("version")
    private int version;
}
