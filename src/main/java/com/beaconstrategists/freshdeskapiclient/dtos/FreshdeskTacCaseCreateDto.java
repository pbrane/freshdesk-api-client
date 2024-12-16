package com.beaconstrategists.freshdeskapiclient.dtos;

import com.beaconstrategists.taccaseapiservice.controllers.dto.TacCaseCreateDto;
import com.beaconstrategists.taccaseapiservice.model.CasePriorityEnum;
import com.beaconstrategists.taccaseapiservice.model.CaseStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
//@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)  //fixme: check on this
public class FreshdeskTacCaseCreateDto extends TacCaseCreateDto {
    private String key;
    private Long ticket;
}
