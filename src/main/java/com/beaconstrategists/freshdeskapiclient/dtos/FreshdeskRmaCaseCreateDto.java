package com.beaconstrategists.freshdeskapiclient.dtos;

import com.beaconstrategists.taccaseapiservice.controllers.dto.RmaCaseCreateDto;
import com.beaconstrategists.taccaseapiservice.controllers.dto.TacCaseCreateDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
//@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)  //fixme: check on this
public class FreshdeskRmaCaseCreateDto extends RmaCaseCreateDto {
    private String key;
    private String tacCase; //fixme: probably not the field we need
}
