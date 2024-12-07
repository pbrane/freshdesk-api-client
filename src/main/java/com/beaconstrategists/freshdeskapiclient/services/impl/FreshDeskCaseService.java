package com.beaconstrategists.freshdeskapiclient.services.impl;

import com.beaconstrategists.freshdeskapiclient.dtos.*;
import com.beaconstrategists.freshdeskapiclient.model.*;
import com.beaconstrategists.freshdeskapiclient.services.CompanyService;
import com.beaconstrategists.freshdeskapiclient.services.SchemaService;
import com.beaconstrategists.taccaseapiservice.controllers.dto.*;
import com.beaconstrategists.taccaseapiservice.model.CasePriorityEnum;
import com.beaconstrategists.taccaseapiservice.model.CaseStatus;
import com.beaconstrategists.taccaseapiservice.services.TacCaseService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

@Service
public class FreshDeskCaseService implements TacCaseService {

    private final RestClient restClient;
    private final CompanyService companyService;
    private final ObjectMapper objectMapper;

    @Value("${FD_CUSTOMER_NAME:Microsoft}")
    private String companyName;

    @Value("${FD_DEFAULT_RESPONDER_ID:3043029172572}")
    private String defaultResponderId;

    private final String tacCaseSchemaId;

    public FreshDeskCaseService(@Qualifier("snakeCaseRestClient") RestClient restClient,
                                SchemaService schemaService,
                                CompanyService companyService,
                                @Qualifier("snakeCaseObjectMapper") ObjectMapper objectMapper) {

        this.restClient = restClient;
        this.companyService = companyService;
        this.objectMapper = objectMapper;
        this.tacCaseSchemaId = schemaService.getSchemaIdByName("TAC Cases");
    }

    
    public TacCaseResponseDto create(TacCaseCreateDto tacCaseCreateDto) {

        TicketCreateDto dto = buildCreateTicket(tacCaseCreateDto, defaultResponderId);

        TicketResponseDto ticket = createTicket(dto, restClient);
        assert ticket != null;  //fixme: what happens here if null

        TicketTacCaseDto ticketTacCaseDto = buildTicketTacCaseDto(tacCaseCreateDto, ticket);
        TacCaseRequest tacCaseRequest = new TacCaseRequest(ticketTacCaseDto);

        TacCaseResponse responseTacCase = createTacCase(tacCaseSchemaId, tacCaseRequest, restClient);

        return mapToTacCaseDto(responseTacCase, ticket);

    }

    @Override
    public TacCaseResponseDto update(Long id, TacCaseUpdateDto tacCaseUpdateDto) {

        //first fetch the existing ticket ??
        //this is where ModelMapper is needed... skip nulls


        return null;
    }

    @Override
    public List<TacCaseResponseDto> findAll() {
        return List.of();
    }

    @Override
    public Optional<TacCaseResponseDto> findById(Long id) {
        return Optional.empty();
    }

    @Override
    public boolean exists(Long id) {
        return false;
    }

    @Override
    public boolean exists(String caseNumber) {
        return false;
    }

    @Override
    public void delete(Long id) {

    }

    /*
     * Add attachments to an existing ticket
     * fixme: this should be addAttachment perhaps?
     */
    public TicketResponseDto addAttachments(Long ticketId, List<TicketAttachmentUploadDto> attachments) {
        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
        attachments.forEach(attachment -> {
            bodyBuilder.part("attachments[]", attachment.getFile().getResource())
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "form-data; name=\"attachments[]\"; filename=\"" + attachment.getFile().getOriginalFilename() + "\"");
        });

        MultiValueMap<String, HttpEntity<?>> multipartBody = bodyBuilder.build();

        try {
            return restClient.put()
                    .uri("/tickets/{ticketId}", ticketId)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(multipartBody)
                    .retrieve()
                    .body(TicketResponseDto.class);
        } catch (Exception e) {
            throw new RuntimeException("Error adding attachments to ticket: " + e.getMessage(), e);
        }
    }


    @Override
    public TacCaseAttachmentResponseDto addAttachment(Long caseId, TacCaseAttachmentUploadDto uploadDto) throws IOException {
        return null;
    }

    @Override
    public List<TacCaseAttachmentResponseDto> getAllAttachments(Long caseId) {
        return List.of();
    }

    @Override
    public void getAttachment(Long caseId, Long attachmentId) {

    }

    @Override
    public TacCaseAttachmentDownloadDto getAttachmentDownload(Long caseId, Long attachmentId) {
        return null;
    }

    @Override
    public void deleteAttachment(Long caseId, Long attachmentId) {

    }

    @Override
    public void deleteAllAttachments(Long caseId) {

    }

    @Override
    public TacCaseNoteResponseDto addNote(Long caseId, TacCaseNoteUploadDto uploadDto) throws IOException {
        return null;
    }

    @Override
    public List<TacCaseNoteResponseDto> getAllNotes(Long caseId) {
        return List.of();
    }

    @Override
    public TacCaseNoteDownloadDto getNote(Long caseId, Long noteId) {
        return null;
    }

    @Override
    public void deleteNote(Long caseId, Long noteId) {

    }

    @Override
    public void deleteAllNotes(Long caseId) {

    }

    @Override
    public List<TacCaseResponseDto> listTacCases(OffsetDateTime caseCreateDateFrom, OffsetDateTime caseCreateDateTo, OffsetDateTime caseCreateDateSince, List<CaseStatus> caseStatus, String logic) {
        return List.of();
    }

    @Override
    public List<RmaCaseResponseDto> listRmaCases(Long id) {
        return List.of();
    }






    /*
    Helper Methods
     */

    private static TacCaseResponse createTacCase(String tacCaseSchemaId, TacCaseRequest tacCaseRequest, RestClient restClient) {
        TacCaseResponse responseTacCase = restClient.post()
                .uri("/custom_objects/schemas/{schemaId}/records", tacCaseSchemaId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(tacCaseRequest)
                .retrieve()
                .body(TacCaseResponse.class);
        assert responseTacCase != null;
        return responseTacCase;
    }

    private static TicketCreateDto buildCreateTicket(TacCaseCreateDto tacCaseDto, String responderId) {

        PriorityForTickets priorityForTickets = PriorityForTickets.valueOf(tacCaseDto.getCasePriority().getValue());

        priorityForTickets = Optional.ofNullable(tacCaseDto.getCasePriority())
                .map(CasePriorityEnum::getValue)
                .map(PriorityForTickets::valueOf)
                .orElse(null);

        //fixme?
        return TicketCreateDto.builder()
                .email(tacCaseDto.getContactEmail())
                .subject(tacCaseDto.getSubject())
                .responderId(Long.valueOf(responderId))
                .type("Problem")
                .source(Source.Email) //fixme ?
                .status(StatusForTickets.Open)
                .priority(Optional.ofNullable(tacCaseDto.getCasePriority()) //fixme?
                        .map(CasePriorityEnum::getValue)
                        .map(PriorityForTickets::valueOf)
                        .orElse(null))
                .description(tacCaseDto.getProblemDescription())
                .build();
    }

    private static TicketResponseDto createTicket(TicketCreateDto dto, RestClient restClient) {
        return restClient.post()
                .uri("/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .body(dto)
                .retrieve()
                .body(TicketResponseDto.class);
    }
    
    private static void debugObjectMappingOfRequest(TacCaseRequest tacCaseRequest, ObjectMapper objectMapper) {
        String json;
        try {
            json = objectMapper.writeValueAsString(tacCaseRequest);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Serialized JSON: " + json);
    }
    
    private static void debugObjectMappingOfDto(TicketCreateDto dto, ObjectMapper objectMapper) {
        String json;
        try {
            json = objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Serialized JSON: " + json);
    }

    //fixme: this is probably going to now have to be done with ModelMapper
    private static TacCaseResponseDto mapToTacCaseDto(TacCaseResponse tacCaseResponse, TicketResponseDto ticket) {
        TicketTacCaseDto data = tacCaseResponse.getData();

        TacCaseResponseDto tacCaseDto = new TacCaseResponseDto();
        tacCaseDto.setSubject(ticket.getSubject());
        tacCaseDto.setId(ticket.getId()); // Extract ID
        tacCaseDto.setProblemDescription(data.getProblemDescription());
        tacCaseDto.setCaseSolutionDescription(data.getCaseSolutionDescription());
        tacCaseDto.setAccountNumber(data.getAccountNumber());
        tacCaseDto.setRmaNeeded(data.getRmaNeeded());
        tacCaseDto.setInstallationCountry(data.getInstallationCountry());
        tacCaseDto.setCustomerTrackingNumber(data.getCustomerTrackingNumber());
        tacCaseDto.setBusinessImpact(data.getBusinessImpact());
        tacCaseDto.setProductName(data.getProductName());
        tacCaseDto.setCasePriority(data.getCasePriority());
        tacCaseDto.setRelatedDispatchCount(data.getRelatedDispatchCount());
        tacCaseDto.setContactEmail(data.getContactEmail());
        tacCaseDto.setCaseClosedDate(safeOffsetDateTime(data.getCaseCloseDate()));
        tacCaseDto.setFirstResponseDate(safeOffsetDateTime(data.getFirstResponseDate()));
        tacCaseDto.setCaseStatus(data.getCaseStatus());
        tacCaseDto.setFaultySerialNumber(data.getFaultySerialNumber());
        tacCaseDto.setProductSerialNumber(data.getProductSerialNumber());
        tacCaseDto.setCaseCreatedDate(safeOffsetDateTime(data.getCaseCreateDate()));
        tacCaseDto.setProductFirmwareVersion(data.getProductFirmwareVersion());

        return tacCaseDto;
    }

    private static OffsetDateTime safeOffsetDateTime(Object dateValue) {
        switch (dateValue) {
            case null -> {
                return null;
            }
            case OffsetDateTime offsetDateTime -> {
                return offsetDateTime;
            }
            case LocalDate localDate -> {
                // Convert LocalDate to OffsetDateTime at the start of the day in UTC
                return localDate.atStartOfDay().atOffset(ZoneOffset.UTC);
                // Convert LocalDate to OffsetDateTime at the start of the day in UTC
            }
            case String s -> {
                // Try parsing the string as a date
                try {
                    return OffsetDateTime.parse((String) dateValue);
                } catch (DateTimeParseException e) {
                    throw new IllegalArgumentException("Cannot parse date string: " + dateValue, e);
                }
                // Try parsing the string as a date
            }
            default -> {
            }
        }
        throw new IllegalArgumentException("Unexpected date type: " + dateValue.getClass());
    }

    //fixme: should use ModelMapper for this
    private static TicketTacCaseDto buildTicketTacCaseDto(TacCaseCreateDto tacCaseDto, TicketResponseDto ticket) {
        return TicketTacCaseDto.builder()
                .casePriority(tacCaseDto.getCasePriority())
                .caseStatus(null)
                .accountNumber(null)
                .businessImpact(tacCaseDto.getBusinessImpact())
                .caseCloseDate(null) //fixme: this should be caseClosedDate (..ed..)
                .caseCreateDate(null) //fixme: this is supposed to be caseCreatedDate (..ed..)
                .caseSolutionDescription(null)
                .contactEmail(tacCaseDto.getContactEmail())
                .key("ID: " + ticket.getId() + "; " + tacCaseDto.getSubject())
                .ticket(ticket.getId())
                .customerTrackingNumber(tacCaseDto.getCustomerTrackingNumber())
                .faultySerialNumber(null)
                .firstResponseDate(null)
                .installationCountry(tacCaseDto.getInstallationCountry())
                .problemDescription(tacCaseDto.getProblemDescription())
                .productFirmwareVersion(tacCaseDto.getProductFirmwareVersion())
                .productName(tacCaseDto.getProductName())
                .productSerialNumber(Optional.ofNullable(tacCaseDto.getProductSerialNumber())
                        .map(Object::toString)
                        .orElse(null))
//                .relatedDispatchCount(tacCaseDto.getRelatedDispatchCount()) //fixme: this should be deleted from schema
/*
                .rmaNeeded(Optional.ofNullable(tacCaseDto.getRmaNeeded())
                        .orElse(false))
*/
                .relatedDispatchCount(null)
                .rmaNeeded(null)
                .build();
    }

    private static TicketUpdateDto buildUpdateTicket(Long id, TacCaseUpdateDto tacCaseDto) {
        return TicketUpdateDto.builder()
                .email(tacCaseDto.getContactEmail())
                .subject(tacCaseDto.getSubject())
                .responderId(Long.getLong("3043029172572")) //fixme
                .type("Problem")
                .source(Source.Email)
                .statusForTickets(StatusForTickets.valueOf(tacCaseDto.getCaseStatus().toString())) //fixme: verify this
                .priorityForTickets(PriorityForTickets.valueOf(tacCaseDto.getCasePriority().toString()))
                .description(tacCaseDto.getProblemDescription())
                .id(id)
                .build();
    }
    
/*    public static CasePriorityEnum mapPriority(PriorityForCustomObjects customPriority) {
        if (customPriority == null) {
            return null;
        }

        return switch (customPriority) {
            case Low -> CasePriorityEnum.Low;
            case Medium -> CasePriorityEnum.Medium;
            case High -> CasePriorityEnum.High;
            case Urgent -> CasePriorityEnum.Urgent;
            default -> throw new IllegalArgumentException("Unknown priority: " + customPriority);
        };
    }

    public static CaseStatus mapStatus(StatusForCustomObjects status) {
        if (status == null) {
            return null;
        }

        return switch (status) {
            case Open -> CaseStatus.Open;
            case Closed -> CaseStatus.Closed;
            case Pending -> CaseStatus.Pending;
            case Resolved -> CaseStatus.Resolved;
            default -> throw new IllegalArgumentException("Unknown status: " + status);
        };
    }
*/
    
}
