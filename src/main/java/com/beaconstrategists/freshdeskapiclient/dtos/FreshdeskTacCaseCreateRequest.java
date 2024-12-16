package com.beaconstrategists.freshdeskapiclient.dtos;

import com.beaconstrategists.taccaseapiservice.controllers.dto.TacCaseCreateDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data //fixme: check this Lombok configuration
public class FreshdeskTacCaseCreateRequest {

    private FreshdeskTacCaseCreateDto data;

    public FreshdeskTacCaseCreateRequest(FreshdeskTacCaseCreateDto data) {
        this.data = data;
    }
}
