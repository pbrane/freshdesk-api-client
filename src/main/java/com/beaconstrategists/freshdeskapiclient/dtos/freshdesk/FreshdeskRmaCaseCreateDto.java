package com.beaconstrategists.freshdeskapiclient.dtos.freshdesk;

import com.beaconstrategists.taccaseapiservice.dtos.RmaCaseCreateDto;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
//@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)  //fixme: check on this
public class FreshdeskRmaCaseCreateDto extends RmaCaseCreateDto {
    private String key;
    private String tacCase;


    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private OffsetDateTime shippedDate;

}
