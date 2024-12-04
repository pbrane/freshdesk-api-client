package com.beaconstrategists.freshdeskapiclient.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TacCaseRequest {

    @JsonProperty("data")
    private TicketTacCaseDto data;

    public TacCaseRequest(TicketTacCaseDto data) {
        this.data = data;
    }
}
