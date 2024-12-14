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
@JsonInclude(JsonInclude.Include.NON_NULL)
//fixme: should probably be renamed to TicketTacCaseResponseDto and mirror the service API DTO
//fixme: or jus wrap the service's DTO and add key or use ModelMapper
public class FreshdeskTacCaseDto {

    private String subject;

//    @JsonProperty("problem_description")
    private String problemDescription;

//    @JsonProperty("case_solution_description")
    private String caseSolutionDescription;

//    @JsonProperty("account_number")
    private String accountNumber;

//    @JsonProperty("rma_needed")
    private Boolean rmaNeeded;

//    @JsonProperty("ticket")
    private Long ticket;

//    @JsonProperty("installation_country")
    private String installationCountry;

//    @JsonProperty("customer_tracking_number")
    private String customerTrackingNumber;

//    @JsonProperty("business_impact")
    private String businessImpact;

//    @JsonProperty("related_dispatch_count")
    private Integer relatedDispatchCount;

//    @JsonProperty("product_name")
    private String productName;

//    @JsonProperty("case_priority")
    private CasePriorityEnum casePriority;

//    @JsonProperty("contact_email")
    private String contactEmail;

//    @JsonProperty("case_close_date")
    private LocalDate caseClosedDate; // Use String or LocalDateTime based on format

//    @JsonProperty("first_response_date")
    private LocalDate firstResponseDate; // Use String or LocalDateTime based on format

//    @JsonProperty("case_status")
    private CaseStatus caseStatus;
//    TicketTacTacCaseStatus caseStatus;

//    @JsonProperty("faulty_serial_number")
    private String faultySerialNumber;

//    @JsonProperty("product_serial_number")
    private String productSerialNumber;

//    @JsonProperty("product_firmware_version")
    private String productFirmwareVersion;

//    @JsonProperty("case_create_date")
    private LocalDate caseCreatedDate; // Use String or LocalDateTime based on format

//    @JsonProperty("key")
    private String key;
}
