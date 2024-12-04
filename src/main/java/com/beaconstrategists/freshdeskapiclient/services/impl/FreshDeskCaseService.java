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
    private final SchemaService schemaService;
    private final CompanyService companyService;
    private final ObjectMapper objectMapper;

    @Value("${FD_CUSTOMER_NAME:Microsoft}")
    private String companyName;

    @Value("${FD_DEFAULT_RESPONDER_ID:3043029172572}")
    private String defaultResponderId;

    public FreshDeskCaseService(@Qualifier("snakeCaseRestClient") RestClient restClient,
                                SchemaService schemaService,
                                CompanyService companyService,
                                @Qualifier("snakeCaseObjectMapper") ObjectMapper objectMapper) {
        this.restClient = restClient;
        this.schemaService = schemaService;
        this.companyService = companyService;
        this.objectMapper = objectMapper;
    }

    public TacCaseDto save(TacCaseDto tacCaseDto) {
        String tacCaseSchemaId = schemaService.getSchemaIdByName("TAC Cases");

        String json = null;

        if (tacCaseDto.getId() == null) {
            //POST with TicketCreateDto containing the ID
            TicketCreateDto dto = buildCreateTicket(tacCaseDto, defaultResponderId);

            try {
                json = objectMapper.writeValueAsString(dto);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            System.out.println("Serialized JSON: " + json);

            TicketResponseDto ticket = restClient.post()
                    .uri("/tickets")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(dto)
                    .retrieve()
                    .body(TicketResponseDto.class);

            assert ticket != null;
            TicketTacCaseDto ticketTacCaseDto = buildTicketTacCaseDto(tacCaseDto, ticket);
            TacCaseRequest tacCaseRequest = new TacCaseRequest(ticketTacCaseDto);

            try {
                json = objectMapper.writeValueAsString(tacCaseRequest);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            System.out.println("Serialized JSON: " + json);

            TacCaseResponse responseTacCase = restClient.post()
                    .uri("/custom_objects/schemas/{schemaId}/records", tacCaseSchemaId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(tacCaseRequest)
                    .retrieve()
                    .body(TacCaseResponse.class);
            assert responseTacCase != null;
            TacCaseDto response = mapToTacCaseDto(responseTacCase, ticket);
            return response;

        } else {
            //PUT with a TicketDto containing the ID
        }
        return null;

    }

    private static TacCaseDto mapToTacCaseDto(TacCaseResponse tacCaseResponse, TicketResponseDto ticket) {
        TicketTacCaseDto data = tacCaseResponse.getData();

        TacCaseDto tacCaseDto = new TacCaseDto();
        tacCaseDto.setId(ticket.getId()); // Extract ID
        tacCaseDto.setProblemDescription(data.getProblemDescription());
        tacCaseDto.setCaseSolutionDescription(data.getCaseSolutionDescription());
        tacCaseDto.setAccountNumber(data.getAccountNumber());
        tacCaseDto.setRmaNeeded(data.getRmaNeeded());
        tacCaseDto.setInstallationCountry(data.getInstallationCountry());
        tacCaseDto.setCustomerTrackingNumber(data.getCustomerTrackingNumber());
        tacCaseDto.setBusinessImpact(data.getBusinessImpact());
        tacCaseDto.setProductName(data.getProductName());
        tacCaseDto.setCasePriority(mapPriority(data.getCasePriority()));
        tacCaseDto.setRelatedDispatchCount(data.getRelatedDispatchCount());
        tacCaseDto.setContactEmail(data.getContactEmail());
        tacCaseDto.setCaseClosedDate(safeOffsetDateTime(data.getCaseCloseDate()));
        tacCaseDto.setFirstResponseDate(safeOffsetDateTime(data.getFirstResponseDate()));
        tacCaseDto.setCaseStatus(mapStatus(data.getCaseStatus()));
        tacCaseDto.setFaultySerialNumber(data.getFaultySerialNumber());
        tacCaseDto.setProductSerialNumber(data.getProductSerialNumber());
        tacCaseDto.setCaseCreatedDate(safeOffsetDateTime(data.getCaseCreateDate()));
        tacCaseDto.setProductFirmwareVersion(data.getProductFirmwareVersion());

        return tacCaseDto;
    }

    private static OffsetDateTime safeOffsetDateTime(Object dateValue) {
        if (dateValue == null) {
            return null;
        }
        if (dateValue instanceof OffsetDateTime) {
            return (OffsetDateTime) dateValue;
        }
        if (dateValue instanceof LocalDate) {
            // Convert LocalDate to OffsetDateTime at the start of the day in UTC
            return ((LocalDate) dateValue).atStartOfDay().atOffset(ZoneOffset.UTC);
        }
        if (dateValue instanceof String) {
            // Try parsing the string as a date
            try {
                return OffsetDateTime.parse((String) dateValue);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Cannot parse date string: " + dateValue, e);
            }
        }
        throw new IllegalArgumentException("Unexpected date type: " + dateValue.getClass());
    }

    private static TicketTacCaseDto buildTicketTacCaseDto(TacCaseDto tacCaseDto, TicketResponseDto ticket) {
        return TicketTacCaseDto.builder()
//                .casePriorityForTickets(Optional.ofNullable(tacCaseDto.getCasePriority())
//                        .map(CasePriorityEnum::getValue)  // Get the string value from CasePriorityEnum
//                        .map(PriorityForTickets::fromString)        // Map to Priority
//                        .orElse(null))
                .casePriority(Optional.ofNullable(tacCaseDto.getCasePriority())
                        .map(CasePriorityEnum::getValue)
                        .map(PriorityForCustomObjects::valueOf)
                        .orElse(null))
                .caseStatus(Optional.ofNullable(tacCaseDto.getCaseStatus())
                        .map(CaseStatus::getValue)          // Get the string value from CaseStatus
                        .map(StatusForCustomObjects::valueOf)              // Map to Status
                        .orElse(null))
                .accountNumber(tacCaseDto.getAccountNumber())
                .businessImpact(tacCaseDto.getBusinessImpact())
                .caseCloseDate(Optional.ofNullable(tacCaseDto.getCaseClosedDate())
                        .map(OffsetDateTime::toLocalDate)
                        .orElse(null))
                .caseCreateDate(Optional.ofNullable(tacCaseDto.getCaseCreatedDate())
                        .map(OffsetDateTime::toLocalDate)
                        .orElse(null))
                .caseSolutionDescription(tacCaseDto.getCaseSolutionDescription())
                .contactEmail(tacCaseDto.getContactEmail())
                .key("ID: " + ticket.getId() + "; " + tacCaseDto.getSubject())
                .ticket(ticket.getId())
                .customerTrackingNumber(tacCaseDto.getCustomerTrackingNumber())
                .faultySerialNumber(tacCaseDto.getFaultySerialNumber())
                .firstResponseDate(Optional.ofNullable(tacCaseDto.getFirstResponseDate())
                        .map(OffsetDateTime::toLocalDate)
                        .orElse(null))
                .installationCountry(tacCaseDto.getInstallationCountry())
                .problemDescription(tacCaseDto.getProblemDescription())
                .productFirmwareVersion(tacCaseDto.getProductFirmwareVersion())
                .productName(tacCaseDto.getProductName())
                .productSerialNumber(Optional.ofNullable(tacCaseDto.getProductSerialNumber())
                        .map(Object::toString)
                        .orElse(null))
                .relatedDispatchCount(tacCaseDto.getRelatedDispatchCount())
                .rmaNeeded(Optional.ofNullable(tacCaseDto.getRmaNeeded())
                        .orElse(false))
                .build();
    }

    @Override
    public List<TacCaseDto> findAll() {
        return List.of();
    }

    @Override
    public Optional<TacCaseDto> findById(Long id) {
        return Optional.empty();
    }

    @Override
    public Optional<TacCaseDto> findByCaseNumber(String caseNumber) {
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
    public TacCaseDto partialUpdate(Long id, TacCaseDto tacCaseDto) {
        return null;
    }

    @Override
    public TacCaseDto partialUpdate(String caseNumber, TacCaseDto tacCaseDto) {
        return null;
    }

    @Override
    public void delete(Long id) {

    }

    @Override
    public void delete(String caseNumber) {

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
    public List<TacCaseDto> listTacCases(OffsetDateTime caseCreateDateFrom, OffsetDateTime caseCreateDateTo, OffsetDateTime caseCreateDateSince, List<CaseStatus> caseStatus, String logic) {
        return List.of();
    }

    @Override
    public List<RmaCaseDto> listRmaCases(Long id) {
        return List.of();
    }

    //Helper methods

    public static CasePriorityEnum mapPriority(PriorityForCustomObjects customPriority) {
        if (customPriority == null) {
            return null;
        }

        return switch (customPriority) {
            case Low -> CasePriorityEnum.LOW;
            case Medium -> CasePriorityEnum.MEDIUM;
            case High -> CasePriorityEnum.HIGH;
            case Urgent -> CasePriorityEnum.URGENT;
            default -> throw new IllegalArgumentException("Unknown priority: " + customPriority);
        };
    }

    public static CaseStatus mapStatus(StatusForCustomObjects status) {
        if (status == null) {
            return null;
        }

        return switch (status) {
            case Open -> CaseStatus.OPEN;
            case Closed -> CaseStatus.CLOSED;
            case Pending -> CaseStatus.PENDING;
            case Resolved -> CaseStatus.RESOLVED;
            default -> throw new IllegalArgumentException("Unknown status: " + status);
        };
    }

    private static TicketCreateDto buildCreateTicket(TacCaseDto tacCaseDto, String responderId) {

        TicketCreateDto ticketDto = TicketCreateDto.builder()
                .email(tacCaseDto.getContactEmail())
                .subject(tacCaseDto.getSubject())
                .responderId(Long.valueOf(responderId))
                .type("Problem")
                .source(Source.Email) //fixme
                .status(StatusForTickets.valueOf(tacCaseDto.getCaseStatus().toString())) //fixme
                .status(Optional.ofNullable(tacCaseDto.getCaseStatus())
                        .map(CaseStatus::getValue)
                        .map(StatusForTickets::valueOf)
                        .orElse(null))
                .priority(Optional.ofNullable(tacCaseDto.getCasePriority())
                        .map(CasePriorityEnum::getValue)
                        .map(PriorityForTickets::valueOf)
                        .orElse(null))
                .description(tacCaseDto.getProblemDescription())
                .build();
        return ticketDto;
    }
    private static TicketUpdateDto buildUpdateTicket(TacCaseDto tacCaseDto) {
        return TicketUpdateDto.builder()
                .email(tacCaseDto.getContactEmail())
                .subject(tacCaseDto.getSubject())
                .responderId(Long.getLong("3043029172572")) //fixme
                .type("Problem")
                .source(Source.Email)
                .statusForTickets(StatusForTickets.valueOf(tacCaseDto.getCaseStatus().toString())) //fixme: verify this
                .priorityForTickets(PriorityForTickets.valueOf(tacCaseDto.getCasePriority().toString()))
                .description(tacCaseDto.getProblemDescription())
                .id(tacCaseDto.getId())
                .build();
    }

    /*
     * Add attachments to an existing ticket
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
}
