package com.beaconstrategists.freshdeskapiclient.services.impl;

import com.beaconstrategists.freshdeskapiclient.dtos.*;
import com.beaconstrategists.freshdeskapiclient.mappers.GenericMapper;
import com.beaconstrategists.freshdeskapiclient.model.*;
import com.beaconstrategists.freshdeskapiclient.services.CompanyService;
import com.beaconstrategists.freshdeskapiclient.services.SchemaService;
import com.beaconstrategists.taccaseapiservice.controllers.dto.*;
import com.beaconstrategists.taccaseapiservice.model.CasePriorityEnum;
import com.beaconstrategists.taccaseapiservice.model.CaseStatus;
import com.beaconstrategists.taccaseapiservice.services.TacCaseService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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

    //fixme: make a method for using this if needed for debugging
    //fixme: or Shell Commands, etc.
    private final ObjectMapper objectMapper;

    private final GenericMapper genericMapper;

    @Value("${FD_CUSTOMER_NAME:Microsoft}")
    private String companyName;

    @Value("${FD_DEFAULT_RESPONDER_ID:3043029172572}")
    private String defaultResponderId;

    private final SchemaService schemaService;

    public FreshDeskCaseService(@Qualifier("snakeCaseRestClient") RestClient restClient,
                                SchemaService schemaService,
                                CompanyService companyService,
                                @Qualifier("snakeCaseObjectMapper") ObjectMapper objectMapper, GenericMapper genericMapper) {

        this.restClient = restClient;
        this.companyService = companyService;
        this.objectMapper = objectMapper;
        this.genericMapper = genericMapper;
        this.schemaService = schemaService;
    }

    
    public TacCaseResponseDto create(TacCaseCreateDto tacCaseCreateDto) {

        FreshdeskTicketCreateDto freshdeskTicketCreateDto = buildCreateTicket(tacCaseCreateDto, defaultResponderId);

        FreshdeskTicketResponseDto freshdeskTicketResponseDto = saveFreshdeskTicket(freshdeskTicketCreateDto, restClient);
        assert freshdeskTicketResponseDto != null;  //fixme: what happens here if null

        //fixme: should we be using the generic mapper here, too?
        FreshdeskTacCaseDto freshdeskTacCaseDto = buildFreshdeskTacCaseCreateDto(tacCaseCreateDto, freshdeskTicketResponseDto);
        FreshdeskTacCaseRequest freshdeskTacCaseRequest = new FreshdeskTacCaseRequest(freshdeskTacCaseDto);

        String tacCaseSchemaId = schemaService.getSchemaIdByName("TAC Cases");
        FreshdeskTacCaseResponse freshdeskTacCaseResponse = saveFreshdeskTacCase(tacCaseSchemaId, freshdeskTacCaseRequest, restClient);

        //fixme: should we be using model mapper?
        //fixme: try new generic mapper at some point!
        //return mapToTacCaseDto(freshdeskTacCaseResponse, freshdeskTicketResponseDto);

        //fixme: should find a way to use the API version of this
        //fixme: and add the extra bits needed for the custom object
        //fixme: in freshdesk
        FreshdeskTacCaseDto data = freshdeskTacCaseResponse.getData();
        TacCaseResponseDto responseDto = genericMapper.map(data, TacCaseResponseDto.class);
        responseDto.setSubject(freshdeskTicketResponseDto.getSubject());
        responseDto.setId(freshdeskTicketResponseDto.getId());
        return responseDto;

    }

    @Override
    public TacCaseResponseDto update(Long id, TacCaseUpdateDto tacCaseUpdateDto) {

        /*
        I have to do the same thing here in the mappings for the ticket
        that I had to do for the TacCase.
         */


        //build a TicketupdateDto based on the user specified fields in the TacCaseUpdateDto
        ObjectNode ticket = objectMapper.createObjectNode();
        //ticket.put();


        TicketUpdateDto.TicketUpdateDtoBuilder builder = TicketUpdateDto.builder();

        TicketUpdateDto ticketDto = builder
                .subject(tacCaseUpdateDto.getSubject())
                .email(tacCaseUpdateDto.getContactEmail())
                .description(tacCaseUpdateDto.getProblemDescription())
                .status(StatusForTickets.valueOf(tacCaseUpdateDto.getCaseStatus().getValue()))
                .priority(PriorityForTickets.valueOf(tacCaseUpdateDto.getCasePriority().getValue()))
                .build();

        FreshdeskTicketResponseDto freshdeskTicketResponseDto = restClient.put()
                .uri("/tickets/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ticketDto)
                .retrieve()
                .body(FreshdeskTicketResponseDto.class);

        assert freshdeskTicketResponseDto != null;
        Long ticketId = freshdeskTicketResponseDto.getId();

//        TacCaseResponseDto caseResponseDto = restClient.get()
//                .uri("/custom_objects/schemas/10563011/records?ticket={ticketId}", ticketId)
//                .body(null)
//                .retrieve()
//                .body(TacCaseResponseDto.class);


        TacCaseResponseDto responseDto = restClient.put()
                .uri("/custom_objects/schemas/schema-id/records/{record-id}", id)//fixme: first find the case matching the ticket
                .contentType(MediaType.APPLICATION_JSON)
                .body(tacCaseUpdateDto)
                .retrieve()
                .body(TacCaseResponseDto.class);


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
    public void delete(Long id) {

    }

    /*
     * Add attachments to an existing ticket
     * fixme: this should be addAttachment perhaps?
     */
    public FreshdeskTicketResponseDto addAttachments(Long ticketId, List<TicketAttachmentUploadDto> attachments) {
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
                    .body(FreshdeskTicketResponseDto.class);
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

    private static FreshdeskTacCaseResponse saveFreshdeskTacCase(String tacCaseSchemaId, FreshdeskTacCaseRequest freshdeskTacCaseRequest, RestClient restClient) {
        FreshdeskTacCaseResponse responseTacCase = restClient.post()
                .uri("/custom_objects/schemas/{schemaId}/records", tacCaseSchemaId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(freshdeskTacCaseRequest)
                .retrieve()
                .body(FreshdeskTacCaseResponse.class);
        assert responseTacCase != null;
        return responseTacCase;
    }

    private static FreshdeskTicketCreateDto buildCreateTicket(TacCaseCreateDto tacCaseDto, String responderId) {

        return FreshdeskTicketCreateDto.builder()
                .email(tacCaseDto.getContactEmail())
                .subject(tacCaseDto.getSubject())
                .responderId(Long.valueOf(responderId))
                .type("Problem")
                .source(Source.Email)
                .status(StatusForTickets.Open)
                .priority(Optional.ofNullable(tacCaseDto.getCasePriority()) //fixme?
                        .map(CasePriorityEnum::getValue)
                        .map(PriorityForTickets::valueOf)
                        .orElse(null))
                .description(tacCaseDto.getProblemDescription())
                .build();
    }

    private static TicketUpdateDto buildUpdateTicket(TacCaseUpdateDto tacCaseUpdateDto) {
        return null;
    }

    private static FreshdeskTicketResponseDto saveFreshdeskTicket(FreshdeskTicketCreateDto dto, RestClient restClient) {
        return restClient.post()
                .uri("/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .body(dto)
                .retrieve()
                .body(FreshdeskTicketResponseDto.class);
    }
    
    private static void debugObjectMappingOfRequest(FreshdeskTacCaseRequest freshdeskTacCaseRequest, ObjectMapper objectMapper) {
        String json;
        try {
            json = objectMapper.writeValueAsString(freshdeskTacCaseRequest);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Serialized JSON: " + json);
    }
    
    private static void debugObjectMappingOfDto(FreshdeskTicketCreateDto dto, ObjectMapper objectMapper) {
        String json;
        try {
            json = objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Serialized JSON: " + json);
    }

    //fixme: this is probably going to now have to be done with ModelMapper
    private static TacCaseResponseDto mapToTacCaseDto(FreshdeskTacCaseResponse freshdeskTacCaseResponse, FreshdeskTicketResponseDto ticket) {
        FreshdeskTacCaseDto data = freshdeskTacCaseResponse.getData();

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
        tacCaseDto.setCaseClosedDate(safeOffsetDateTime(data.getCaseClosedDate()));
        tacCaseDto.setFirstResponseDate(safeOffsetDateTime(data.getFirstResponseDate()));
        tacCaseDto.setCaseStatus(data.getCaseStatus());
        tacCaseDto.setFaultySerialNumber(data.getFaultySerialNumber());
        tacCaseDto.setProductSerialNumber(data.getProductSerialNumber());
        tacCaseDto.setCaseCreatedDate(safeOffsetDateTime(data.getCaseCreatedDate()));
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

    //fixme: should use Generic ModelMapper for this
    private static FreshdeskTacCaseDto buildFreshdeskTacCaseCreateDto(TacCaseCreateDto tacCaseDto, FreshdeskTicketResponseDto ticket) {
        return FreshdeskTacCaseDto.builder()
                .subject(tacCaseDto.getSubject())
                .casePriority(tacCaseDto.getCasePriority())
                .caseStatus(CaseStatus.valueOf(ticket.getStatusForTickets().name()))
                .accountNumber(null)
                .businessImpact(tacCaseDto.getBusinessImpact())
                .caseClosedDate(null)
                .caseCreatedDate(ticket.getCreatedAt().toLocalDate())
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
                .productSerialNumber(tacCaseDto.getProductSerialNumber())
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
                .status(StatusForTickets.valueOf(tacCaseDto.getCaseStatus().toString())) //fixme: verify this
                .priority(PriorityForTickets.valueOf(tacCaseDto.getCasePriority().toString()))
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
