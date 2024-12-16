package com.beaconstrategists.freshdeskapiclient.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)  //fixme: check this what we want/need here
public class FreshdeskTacCaseResponseRecords {

    private List<FreshdeskTacCaseResponse> records;

}
