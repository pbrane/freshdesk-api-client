package com.beaconstrategists.freshdeskapiclient.dtos;

import com.beaconstrategists.taccaseapiservice.model.CasePriorityEnum;
import com.beaconstrategists.taccaseapiservice.model.CaseStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) //fixme: check this is needed or correct
//fixme: figure out why this doesn't serialize the full list of fields if it extends the API's TacCaseResponseDto
public class FreshdeskTacCaseResponseDto {

    private String subject;

    private String problemDescription;

    private String caseSolutionDescription;

    private String accountNumber;

    private Boolean rmaNeeded;

    private String installationCountry;

    private String customerTrackingNumber;

    private String businessImpact;

    private Integer relatedDispatchCount;

    private String productName;

    private CasePriorityEnum casePriority;

    private String contactEmail;

    private LocalDate caseClosedDate;

    private LocalDate firstResponseDate;

    private CaseStatus caseStatus;

    private String faultySerialNumber;

    private String productSerialNumber;

    private String productFirmwareVersion;

    private LocalDate caseCreatedDate;

    /**
     * This field mirrors a must have configuration in Freshdesk, I used the term Key,
     * which is required for this configuration, but is just the name given for the
     * Primary Field that is required for all Freshdesk Custom Objects.
     */
    private String key;

    private Long ticket;

}
