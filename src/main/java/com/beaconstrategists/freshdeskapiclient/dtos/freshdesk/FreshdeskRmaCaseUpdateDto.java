package com.beaconstrategists.freshdeskapiclient.dtos.freshdesk;

import com.beaconstrategists.taccaseapiservice.dtos.RmaCaseUpdateDto;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;
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
public class FreshdeskRmaCaseUpdateDto extends RmaCaseUpdateDto {

    private String key;
    @JsonSetter
    public void setKey(String value) {
        this.key = value;
        markFieldPresent("key");
    }

    private String tacCase;
    @JsonSetter
    public void setTacCase(String value) {
        this.tacCase = value;
        markFieldPresent("tacCase");
    }

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private OffsetDateTime shippedDate;

}
