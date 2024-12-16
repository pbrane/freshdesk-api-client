package com.beaconstrategists.freshdeskapiclient.services.impl;

import com.beaconstrategists.freshdeskapiclient.dtos.*;
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


    /**
     * Create a TAC Case in Freshdesk
     */
    public TacCaseResponseDto create(TacCaseCreateDto tacCaseCreateDto) {

        FreshdeskTicketCreateDto freshdeskTicketCreateDto = buildCreateTicket(tacCaseCreateDto, defaultResponderId);

        FreshdeskTicketResponseDto freshdeskTicketResponseDto = saveFreshdeskTicket(freshdeskTicketCreateDto, snakeCaseRestClient);

        assert freshdeskTicketResponseDto != null;  //fixme: what happens here if null

        FreshdeskTacCaseCreateDto freshdeskTacCaseCreateDto = genericModelMapper.map(tacCaseCreateDto, FreshdeskTacCaseCreateDto.class);
        freshdeskTacCaseCreateDto.setKey("ID: " + freshdeskTicketResponseDto.getId() + "; " + tacCaseCreateDto.getSubject());
        freshdeskTacCaseCreateDto.setTicket(freshdeskTicketResponseDto.getId());
        //fixme: FreshdeskTacCaseCreateRequest and FreshdeskTacCaseRequest are identical
        FreshdeskTacCaseCreateRequest freshdeskTacCaseCreateRequest = new FreshdeskTacCaseCreateRequest(freshdeskTacCaseCreateDto);
        String tacCaseSchemaId = schemaService.getSchemaIdByName("TAC Cases");

        FreshdeskTacCaseResponse freshdeskTacCaseResponse = saveFreshdeskTacCase(tacCaseSchemaId, freshdeskTacCaseCreateRequest, snakeCaseRestClient);

        FreshdeskTacCaseResponseDto data = freshdeskTacCaseResponse.getData();
        TacCaseResponseDto responseDto = genericModelMapper.map(data, TacCaseResponseDto.class);
        responseDto.setSubject(freshdeskTicketResponseDto.getSubject());
        responseDto.setId(freshdeskTicketResponseDto.getId());
        return responseDto;

    }

    @Override
    public TacCaseResponseDto update(Long id, TacCaseUpdateDto tacCaseUpdateDto) {

        //Update the Freshdesk Ticket
        FreshdeskTicketUpdateDto freshdeskTicketUpdateDto = buildFreshdeskTicketUpdateDto(tacCaseUpdateDto);
        FreshdeskTicketResponseDto freshdeskTicketResponseDto = updateTicket(id, freshdeskTicketUpdateDto);

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
        FreshdeskTacCaseResponseRecords freshdeskTacCaseResponseRecords = findFreshdeskTacCaseRecords(id, tacCaseSchemaId);

        assert freshdeskTacCaseResponseRecords != null;

/*
        If there is a record, there will only be one as this is a 1:1 relationship by design
        The record ID here is not an integer like with a ticket
*/
        Optional<FreshdeskTacCaseResponse> record = freshdeskTacCaseResponseRecords.getRecords().stream().findFirst();
        FreshdeskTacCaseResponseDto freshdeskTacCaseResponseDto = record.map(FreshdeskTacCaseResponse::getData).orElse(null);

        //get the record's identifier, this is what we need to update the record
        String displayId = record.get().getDisplayId();

/*
         The Custom Objects API documentation is wrong. One must update all the
         custom object fields with the current values as a partial update isn't
         working on custom objects.
         The following is from the documentation.
         { "display_id": "BKG-1", "data": { "product_name": "Fiber Concentrator", "product_firmware_version": "v1.23.4" } }
*/
        // Map all the fields from the query response to the update dto
        FreshdeskTacCaseUpdateDto freshdeskTacCaseUpdateDto = genericModelMapper.map(freshdeskTacCaseResponseDto, FreshdeskTacCaseUpdateDto.class);

        // Now wrap it to have the display_id, version (also not documented), and the request in an element called data:)
        FreshdeskTacCaseUpdateRequest updateRequest = new FreshdeskTacCaseUpdateRequest(freshdeskTacCaseUpdateDto);
        updateRequest.setDisplayId(displayId);
        updateRequest.setVersion(record.get().getVersion());

        FreshdeskTacCaseResponse response = saveFreshdeskTacCase(tacCaseSchemaId, displayId, updateRequest);

        FreshdeskTacCaseResponseDto responseData = response.getData();
        return genericModelMapper.map(responseData, TacCaseResponseDto.class);
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
    private FreshdeskTacCaseResponse saveFreshdeskTacCase(String tacCaseSchemaId, String displayId, FreshdeskTacCaseUpdateRequest updateRequest) {
        FreshdeskTacCaseResponse response = fieldPresenseRestClient.put()
                .uri("/custom_objects/schemas/{schema-id}/records/{record-id}", tacCaseSchemaId, displayId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(updateRequest)
                .retrieve()
                .body(FreshdeskTacCaseResponse.class);
        return response;
    }

    private FreshdeskTacCaseResponseRecords findFreshdeskTacCaseRecords(Long id, String tacCaseSchemaId) {
        FreshdeskTacCaseResponseRecords freshdeskTacCaseResponseRecords = snakeCaseRestClient.get()
                .uri("/custom_objects/schemas/"+ tacCaseSchemaId +"/records?ticket={ticketId}", id)
                .retrieve()
                .body(FreshdeskTacCaseResponseRecords.class);
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

    private static FreshdeskTacCaseResponse saveFreshdeskTacCase(String tacCaseSchemaId, FreshdeskTacCaseCreateRequest freshdeskTacCaseCreateRequest, RestClient restClient) {
        FreshdeskTacCaseResponse responseTacCase = restClient.post()
                .uri("/custom_objects/schemas/{schemaId}/records", tacCaseSchemaId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(freshdeskTacCaseCreateRequest)
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

    private static FreshdeskTicketResponseDto saveFreshdeskTicket(FreshdeskTicketCreateDto dto, RestClient restClient) {
        return restClient.post()
                .uri("/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .body(dto)
                .retrieve()
                .body(FreshdeskTicketResponseDto.class);
    }

    private static FreshdeskTicketUpdateDto buildFreshdeskTicketUpdateDto(TacCaseUpdateDto tacCaseUpdateDto) {
        FreshdeskTicketUpdateDto freshdeskTicketUpdateDto = new FreshdeskTicketUpdateDto();
        if (tacCaseUpdateDto.isFieldPresent("subject")) {
            freshdeskTicketUpdateDto.setSubject(tacCaseUpdateDto.getSubject());
        }

        if (tacCaseUpdateDto.isFieldPresent("contactEmail")) {
            freshdeskTicketUpdateDto.setEmail(tacCaseUpdateDto.getContactEmail());
        }

        if (tacCaseUpdateDto.isFieldPresent("problemDescription")) {
            freshdeskTicketUpdateDto.setDescription(tacCaseUpdateDto.getProblemDescription());
        }

        if (tacCaseUpdateDto.isFieldPresent("caseStatus")) {
            freshdeskTicketUpdateDto.setStatus(StatusForTickets.valueOf(tacCaseUpdateDto.getCaseStatus().getValue()));

        }

        if (tacCaseUpdateDto.isFieldPresent("casePriority")) {
            freshdeskTicketUpdateDto.setPriority(PriorityForTickets.valueOf(tacCaseUpdateDto.getCasePriority().getValue()));
        }
        return freshdeskTicketUpdateDto;
    }
    
}
