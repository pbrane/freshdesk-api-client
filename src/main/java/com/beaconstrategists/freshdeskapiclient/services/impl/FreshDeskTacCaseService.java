package com.beaconstrategists.freshdeskapiclient.services.impl;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import com.beaconstrategists.freshdeskapiclient.dtos.*;
import com.beaconstrategists.freshdeskapiclient.mappers.FieldPresenceModelMapper;
import com.beaconstrategists.freshdeskapiclient.mappers.GenericModelMapper;
import com.beaconstrategists.freshdeskapiclient.model.*;
import com.beaconstrategists.freshdeskapiclient.services.CompanyService;
import com.beaconstrategists.freshdeskapiclient.services.SchemaService;
import com.beaconstrategists.taccaseapiservice.controllers.dto.*;
import com.beaconstrategists.taccaseapiservice.model.CasePriorityEnum;
import com.beaconstrategists.taccaseapiservice.model.CaseStatus;
import com.beaconstrategists.taccaseapiservice.services.TacCaseService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;


import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class FreshDeskTacCaseService implements TacCaseService {

    private final RestClient snakeCaseRestClient;
    private final RestClient fieldPresenseRestClient;

    private final CompanyService companyService;

    private final GenericModelMapper genericModelMapper;

    @Value("${FD_CUSTOMER_NAME:Microsoft}")
    private String companyName;

    @Value("${FD_DEFAULT_RESPONDER_ID:3043029172572}")
    private String defaultResponderId;

    private final SchemaService schemaService;

    public FreshDeskTacCaseService(@Qualifier("snakeCaseRestClient") RestClient snakeCaseRestClient,
                                   SchemaService schemaService,
                                   CompanyService companyService,
                                   GenericModelMapper genericModelMapper,
                                   @Qualifier("fieldPresenceSnakeCaseSerializingRestClient") RestClient fieldPresenseRestClient) {

        this.snakeCaseRestClient = snakeCaseRestClient;
        this.companyService = companyService;
        this.genericModelMapper = genericModelMapper;
        this.schemaService = schemaService;
        this.fieldPresenseRestClient = fieldPresenseRestClient;
    }


    /*
     * Create an RMA Case in Freshdesk
     */

    public RmaCaseResponseDto create(RmaCaseCreateDto rmaCaseCreateDto) {

        //Find the TAC Case
        Long id = rmaCaseCreateDto.getTacCaseId();
        String tacCaseSchemaId = schemaService.getSchemaIdByName("TAC Cases");
        FreshdeskTacCaseResponseRecords<FreshdeskTacCaseResponseDto> freshdeskTacCaseResponseRecords = findFreshdeskTacCaseRecords(id, tacCaseSchemaId);
        assert freshdeskTacCaseResponseRecords != null;
        Optional<FreshdeskCaseResponse<FreshdeskTacCaseResponseDto>> record = freshdeskTacCaseResponseRecords.getRecords().stream().findFirst();
        FreshdeskTacCaseResponseDto freshdeskTacCaseResponseDto = record.map(FreshdeskCaseResponse::getData).orElse(null);
        //get the record's identifier, this is what we need to update the record
        String displayId = record.get().getDisplayId();
        Long ticketId = freshdeskTacCaseResponseDto.getTicket();
        String rmaKey = freshdeskTacCaseResponseDto.getKey();

        //Create the RMA Case referring to the TAC Case
        FreshdeskRmaCaseCreateDto freshdeskRmaCaseCreateDto = genericModelMapper.map(rmaCaseCreateDto, FreshdeskRmaCaseCreateDto.class);
        Random random = new Random();
        char[] alphabet = {'m','o','l','e','x'};
        int size = 12;
        String randomKey = NanoIdUtils.randomNanoId(random, alphabet, size);
        freshdeskRmaCaseCreateDto.setKey(rmaKey+":"+ randomKey);
        freshdeskRmaCaseCreateDto.setTacCase(displayId);
        FreshdeskDataCreateRequest<FreshdeskRmaCaseCreateDto> freshdeskRmaCreateRequest = new FreshdeskDataCreateRequest<>(freshdeskRmaCaseCreateDto);
        String rmaCaseSchemaId = schemaService.getSchemaIdByName("RMA Cases");

        return null;
    }


    /*
     * Create a TAC Case in Freshdesk
     */
    public TacCaseResponseDto create(TacCaseCreateDto tacCaseCreateDto) {

        FreshdeskTicketCreateDto freshdeskTicketCreateDto = buildCreateTicket(tacCaseCreateDto, defaultResponderId);
        FreshdeskTicketResponseDto freshdeskTicketResponseDto = saveFreshdeskTicket(freshdeskTicketCreateDto, snakeCaseRestClient);

        assert freshdeskTicketResponseDto != null;  //fixme: what happens here if null

        FreshdeskTacCaseCreateDto freshdeskTacCaseCreateDto = genericModelMapper.map(tacCaseCreateDto, FreshdeskTacCaseCreateDto.class);
        freshdeskTacCaseCreateDto.setKey("ID: " + freshdeskTicketResponseDto.getId() + "; " + tacCaseCreateDto.getSubject());
        freshdeskTacCaseCreateDto.setTicket(freshdeskTicketResponseDto.getId());
        //fixme: FreshdeskDataCreateRequest and FreshdeskTacCaseRequest are identical
        FreshdeskDataCreateRequest<FreshdeskTacCaseCreateDto> freshdeskTacCaseCreateRequest = new FreshdeskDataCreateRequest<>(freshdeskTacCaseCreateDto);
        String tacCaseSchemaId = schemaService.getSchemaIdByName("TAC Cases");

        FreshdeskCaseResponse<FreshdeskTacCaseResponseDto> freshdeskTacCaseResponse = updateFreshdeskTacCase(tacCaseSchemaId, freshdeskTacCaseCreateRequest, snakeCaseRestClient);
        FreshdeskTacCaseResponseDto data = freshdeskTacCaseResponse.getData();

        TacCaseResponseDto responseDto = genericModelMapper.map(data, TacCaseResponseDto.class);
        responseDto.setSubject(freshdeskTicketResponseDto.getSubject());
        responseDto.setId(freshdeskTicketResponseDto.getId());
        return responseDto;

    }

    /*
     * Update a TAC Case in Freshdesk
     */

    @Override
    public TacCaseResponseDto update(Long id, TacCaseUpdateDto tacCaseUpdateDto) {

        FreshdeskTicketResponseDto freshdeskTicketResponseDto;

        FreshdeskTicketUpdateDto freshdeskTicketUpdateDto = buildFreshdeskTicketUpdateDto(tacCaseUpdateDto);
        if (freshdeskTicketUpdateDto != null) {
            freshdeskTicketResponseDto = updateTicket(id, freshdeskTicketUpdateDto);
        } else {
            freshdeskTicketResponseDto = snakeCaseRestClient.get()
                    .uri("/tickets/{id}", id)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(FreshdeskTicketResponseDto.class);
        }

        assert freshdeskTicketResponseDto != null; //fixme: what happens here if null?

/*
        Now update the Freshdesk TAC Case Custom Object
        Now we need to find that TAC Case associated with this Freshdesk Ticket
        Need the Schema ID to build the URL
*/
        String tacCaseSchemaId = schemaService.getSchemaIdByName("TAC Cases");

/*
        The response body we are looking for here is the same as the response from
        a create but since a query, by design, could be more than one "record"
        or row, the response comes wrapped in an array called "records".
*/
        FreshdeskTacCaseResponseRecords<FreshdeskTacCaseResponseDto> freshdeskTacCaseResponseRecords = findFreshdeskTacCaseRecords(id, tacCaseSchemaId);
        assert freshdeskTacCaseResponseRecords != null;

/*
        If there is a record, there will only be one as this is a 1:1 relationship by design
        The record ID here is not an integer like with a ticket
*/
        Optional<FreshdeskCaseResponse<FreshdeskTacCaseResponseDto>> record = freshdeskTacCaseResponseRecords.getRecords().stream().findFirst();
        FreshdeskTacCaseResponseDto freshdeskTacCaseResponseDto = record.map(FreshdeskCaseResponse::getData).orElse(null);

        //get the record's identifier, this is what we need to update the record
        String displayId = record.get().getDisplayId();

/*
         The Custom Objects API documentation is wrong. One must update all the
         custom object fields with the current values as a partial update isn't
         working on custom objects.
         The following is from the documentation.
         { "display_id": "BKG-1", "data": { "product_name": "Fiber Concentrator", "product_firmware_version": "v1.23.4" } }
*/
        // Map all the field values from the TAC Case query response to the update dto
        FreshdeskTacCaseUpdateDto freshdeskTacCaseUpdateDto = genericModelMapper.map(freshdeskTacCaseResponseDto, FreshdeskTacCaseUpdateDto.class);
        CasePriorityEnum priority = CasePriorityEnum.valueOf(freshdeskTicketResponseDto.getPriorityForTickets().name());
        freshdeskTacCaseUpdateDto.setCasePriority(priority);
        CaseStatus status = CaseStatus.valueOf(freshdeskTicketResponseDto.getStatusForTickets().name());
        freshdeskTacCaseUpdateDto.setCaseStatus(status);
        freshdeskTacCaseUpdateDto.setSubject(freshdeskTicketResponseDto.getSubject());

        // Now map all the "fields present" in the TacCaseUpdateDto.
        FieldPresenceModelMapper fieldPresenceModelMapper = new FieldPresenceModelMapper();
        fieldPresenceModelMapper.map(tacCaseUpdateDto, freshdeskTacCaseUpdateDto);

        // Now wrap it to have the display_id, version (also not documented), and the request in an element called data:)
        FreshdeskTacCaseUpdateRequest updateRequest = new FreshdeskTacCaseUpdateRequest(freshdeskTacCaseUpdateDto);
        updateRequest.setDisplayId(displayId);
        updateRequest.setVersion(record.get().getVersion());

        FreshdeskCaseResponse<FreshdeskTacCaseResponseDto> response = updateFreshdeskTacCase(tacCaseSchemaId, displayId, updateRequest);

        FreshdeskTacCaseResponseDto responseData = response.getData();
        TacCaseResponseDto tacCaseResponseDto = genericModelMapper.map(responseData, TacCaseResponseDto.class);
        tacCaseResponseDto.setId(freshdeskTicketResponseDto.getId());
        tacCaseResponseDto.setSubject(freshdeskTicketResponseDto.getSubject());
        tacCaseResponseDto.setCaseStatus(status);
        tacCaseResponseDto.setCasePriority(priority);
        return tacCaseResponseDto;
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
            return snakeCaseRestClient.put()
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
    private FreshdeskCaseResponse<FreshdeskTacCaseResponseDto> updateFreshdeskTacCase(String tacCaseSchemaId, String displayId, FreshdeskTacCaseUpdateRequest updateRequest) {
        FreshdeskCaseResponse<FreshdeskTacCaseResponseDto> response = fieldPresenseRestClient.put()
                .uri("/custom_objects/schemas/{schema-id}/records/{record-id}", tacCaseSchemaId, displayId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(updateRequest)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
        return response;
    }

    private FreshdeskTacCaseResponseRecords<FreshdeskTacCaseResponseDto> findFreshdeskTacCaseRecords(Long id, String tacCaseSchemaId) {
        FreshdeskTacCaseResponseRecords<FreshdeskTacCaseResponseDto> freshdeskTacCaseResponseRecords = snakeCaseRestClient.get()
                .uri("/custom_objects/schemas/" + tacCaseSchemaId + "/records?ticket={ticketId}", id)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
        return freshdeskTacCaseResponseRecords;
    }

    private FreshdeskTicketResponseDto updateTicket(Long id, FreshdeskTicketUpdateDto freshdeskTicketUpdateDto) {
        FreshdeskTicketResponseDto freshdeskTicketResponseDto = fieldPresenseRestClient.put()
                .uri("/tickets/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(freshdeskTicketUpdateDto)
                .retrieve()
                .body(FreshdeskTicketResponseDto.class);
        return freshdeskTicketResponseDto;
    }

    private static FreshdeskCaseResponse<FreshdeskRmaCaseResponseDto>
    createFreshdeskRmaCase(String rmaCaseSchemaId,
                           FreshdeskDataCreateRequest<FreshdeskRmaCaseCreateDto> freshdeskRmaCaseCreateRequest,
                           RestClient restClient) {

        FreshdeskCaseResponse<FreshdeskRmaCaseResponseDto> responseRmaCase = restClient.post()
                .uri("/custom_objects/schemas/{schemaId}/records", rmaCaseSchemaId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(freshdeskRmaCaseCreateRequest)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
        assert responseRmaCase != null;
        return responseRmaCase;
    }


    private static FreshdeskCaseResponse<FreshdeskTacCaseResponseDto>
    updateFreshdeskTacCase(String tacCaseSchemaId,
                           FreshdeskDataCreateRequest<FreshdeskTacCaseCreateDto> freshdeskTacCaseCreateRequest,
                           RestClient restClient) {

        FreshdeskCaseResponse<FreshdeskTacCaseResponseDto> responseTacCase = restClient.post()
                .uri("/custom_objects/schemas/{schemaId}/records", tacCaseSchemaId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(freshdeskTacCaseCreateRequest)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
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

    private static FreshdeskTicketResponseDto saveFreshdeskTicket(FreshdeskTicketCreateDto dto, RestClient restClient) {
        return restClient.post()
                .uri("/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .body(dto)
                .retrieve()
                .body(FreshdeskTicketResponseDto.class);
    }

    private static FreshdeskTicketUpdateDto buildFreshdeskTicketUpdateDto(TacCaseUpdateDto tacCaseUpdateDto) {

        boolean requiresUpdate = false;

        FreshdeskTicketUpdateDto freshdeskTicketUpdateDto = new FreshdeskTicketUpdateDto();
        if (tacCaseUpdateDto.isFieldPresent("subject")) {
            freshdeskTicketUpdateDto.setSubject(tacCaseUpdateDto.getSubject());
            requiresUpdate = true;
        }

        if (tacCaseUpdateDto.isFieldPresent("contactEmail")) {
            freshdeskTicketUpdateDto.setEmail(tacCaseUpdateDto.getContactEmail());
            requiresUpdate = true;
        }

        if (tacCaseUpdateDto.isFieldPresent("problemDescription")) {
            freshdeskTicketUpdateDto.setDescription(tacCaseUpdateDto.getProblemDescription());
            requiresUpdate = true;
        }

        if (tacCaseUpdateDto.isFieldPresent("caseStatus")) {
            freshdeskTicketUpdateDto.setStatus(StatusForTickets.valueOf(tacCaseUpdateDto.getCaseStatus().getValue()));
            requiresUpdate = true;
        }

        if (tacCaseUpdateDto.isFieldPresent("casePriority")) {
            freshdeskTicketUpdateDto.setPriority(PriorityForTickets.valueOf(tacCaseUpdateDto.getCasePriority().getValue()));
            requiresUpdate = true;
        }
        if (requiresUpdate) {
            return freshdeskTicketUpdateDto;
        } else {
            return null;
        }
    }
    
}
