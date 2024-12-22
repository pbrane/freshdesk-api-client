package com.beaconstrategists.freshdeskapiclient.services.impl;

import com.beaconstrategists.freshdeskapiclient.dtos.*;
import com.beaconstrategists.freshdeskapiclient.mappers.FieldPresenceModelMapper;
import com.beaconstrategists.freshdeskapiclient.mappers.GenericModelMapper;
import com.beaconstrategists.freshdeskapiclient.model.FreshdeskConversationSource;
import com.beaconstrategists.freshdeskapiclient.model.PriorityForTickets;
import com.beaconstrategists.freshdeskapiclient.model.Source;
import com.beaconstrategists.freshdeskapiclient.model.StatusForTickets;
import com.beaconstrategists.freshdeskapiclient.services.CompanyService;
import com.beaconstrategists.freshdeskapiclient.services.SchemaService;
import com.beaconstrategists.taccaseapiservice.dtos.*;
import com.beaconstrategists.taccaseapiservice.model.CasePriorityEnum;
import com.beaconstrategists.taccaseapiservice.model.CaseStatus;
import com.beaconstrategists.taccaseapiservice.services.TacCaseService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@Service("FreshdeskTacCaseService")
public class FreshDeskTacCaseService implements TacCaseService {

    private final RestClient snakeCaseRestClient;

    /*
     * This RestClient is only needed for updating Tickets
     * Freshdesk Tickets can handle partial updates.
     * Freshdesk Custom Objects (TAC/RMA Cases) require full updates.
     * The Custom Objects are managed with a FieldPresence ModelMapper.
     * So, they don't need, and shouldn't use, this RestClient
     */
    private final RestClient fieldPresenseRestClient;

    private final CompanyService companyService;

    private final GenericModelMapper genericModelMapper;
    private final ObjectMapper snakeCaseObjectMapper;

    @Value("${FD_CUSTOMER_NAME:Beacon}")
    private String companyName;

    @Value("${FD_DEFAULT_RESPONDER_ID:3043029172572}")
    private String defaultResponderId;

    @Value("${FD_TAC_CASE_ID_PREFIX:_2-}")
    private String tacCaseIdPrefix;

    private final SchemaService schemaService;

    public FreshDeskTacCaseService(@Qualifier("snakeCaseRestClient") RestClient snakeCaseRestClient,
                                   SchemaService schemaService,
                                   CompanyService companyService,
                                   GenericModelMapper genericModelMapper,
                                   @Qualifier("fieldPresenceSnakeCaseSerializingRestClient") RestClient fieldPresenseRestClient,
                                   @Qualifier("snakeCaseObjectMapper") ObjectMapper snakeCaseObjectMapper) {

        this.snakeCaseRestClient = snakeCaseRestClient;
        this.companyService = companyService;
        this.genericModelMapper = genericModelMapper;
        this.schemaService = schemaService;
        this.fieldPresenseRestClient = fieldPresenseRestClient;
        this.snakeCaseObjectMapper = snakeCaseObjectMapper;
    }

    /*
     * Create a TAC Case in Freshdesk
     */
    @Override
    public TacCaseResponseDto create(TacCaseCreateDto tacCaseCreateDto) {

        FreshdeskTicketCreateDto freshdeskTicketCreateDto = buildCreateTicket(tacCaseCreateDto, defaultResponderId);
        FreshdeskTicketResponseDto freshdeskTicketResponseDto = createFreshdeskTicket(freshdeskTicketCreateDto);

        assert freshdeskTicketResponseDto != null;  //fixme: what happens here if null

        FreshdeskTacCaseCreateDto freshdeskTacCaseCreateDto = genericModelMapper.map(tacCaseCreateDto, FreshdeskTacCaseCreateDto.class);
        freshdeskTacCaseCreateDto.setKey("ID: " + freshdeskTicketResponseDto.getId() + "; " + tacCaseCreateDto.getSubject());
        freshdeskTacCaseCreateDto.setTicket(freshdeskTicketResponseDto.getId());
        //fixme: FreshdeskDataCreateRequest and FreshdeskTacCaseRequest are identical
        FreshdeskDataCreateRequest<FreshdeskTacCaseCreateDto> freshdeskTacCaseCreateRequest = new FreshdeskDataCreateRequest<>(freshdeskTacCaseCreateDto);

        FreshdeskCaseResponse<FreshdeskTacCaseResponseDto> freshdeskTacCaseResponse = updateFreshdeskTacCase(freshdeskTacCaseCreateRequest);
        FreshdeskTacCaseResponseDto data = freshdeskTacCaseResponse.getData();

        TacCaseResponseDto responseDto = genericModelMapper.map(data, TacCaseResponseDto.class);
        responseDto.setSubject(freshdeskTicketResponseDto.getSubject());
        responseDto.setId(freshdeskTicketResponseDto.getId());

        //fixme: to do this right, we have to do an update with this value
        //responseDto.setCaseNumber(freshdeskTacCaseResponse.getDisplayId());
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

/*
        The response body we are looking for here is the same as the response from
        a create but since a query, by design, could be more than one "record"
        or row, the response comes wrapped in an array called "records".
*/
        FreshdeskTacCaseResponseRecords<FreshdeskTacCaseResponseDto> freshdeskTacCaseResponseRecords =
                findFreshdeskTacCaseRecords(id);
        // I don't think this response will ever be null
        //assert freshdeskTacCaseResponseRecords != null;

/*
        If there is a record, there will only be one as this is a 1:1 relationship by design
        The record ID here is not an integer like with a ticket
*/
        Optional<FreshdeskCaseResponse<FreshdeskTacCaseResponseDto>> freshdeskCaseResponse = freshdeskTacCaseResponseRecords.getRecords().stream().findFirst();
        FreshdeskTacCaseResponseDto freshdeskTacCaseResponseDto = freshdeskCaseResponse.map(FreshdeskCaseResponse::getData).orElse(null);

        //get the record's identifier, this is what we need to update the record
        String tacCaseDisplayId = freshdeskCaseResponse.get().getDisplayId();

/*
         The Custom Objects API documentation is wrong. One must update all the
         custom object fields with the current values as a partial update isn't
         working on custom objects.
         The following is from the documentation.
         { "display_id": "BKG-1", "data": { "product_name": "Fiber Concentrator", "product_firmware_version": "v1.23.4" } }
*/
        // Map all the field values from the TAC Case query response to the update dto
        FreshdeskTacCaseUpdateDto freshdeskTacCaseUpdateDto = genericModelMapper.map(freshdeskTacCaseResponseDto, FreshdeskTacCaseUpdateDto.class);
        //fixme: we probably need to move this out of the TAC Case Object
        CasePriorityEnum priority = CasePriorityEnum.valueOf(freshdeskTicketResponseDto.getPriorityForTickets().name());
        //fixme: we probably need to move this out of the TAC Case Object
        freshdeskTacCaseUpdateDto.setCasePriority(priority);
        CaseStatus status = CaseStatus.valueOf(freshdeskTicketResponseDto.getStatusForTickets().name());
        freshdeskTacCaseUpdateDto.setCaseStatus(status);
        freshdeskTacCaseUpdateDto.setSubject(freshdeskTicketResponseDto.getSubject());

        // Now map all the "fields present" in the TacCaseUpdateDto.
        FieldPresenceModelMapper fieldPresenceModelMapper = new FieldPresenceModelMapper();
        fieldPresenceModelMapper.map(tacCaseUpdateDto, freshdeskTacCaseUpdateDto);

        // Now wrap it to have the display_id, version (also not documented), and the request in an element called data:)
        FreshdeskTacCaseUpdateRequest updateRequest = new FreshdeskTacCaseUpdateRequest(freshdeskTacCaseUpdateDto);
        updateRequest.setDisplayId(tacCaseDisplayId);
        updateRequest.setVersion(freshdeskCaseResponse.get().getVersion());

        FreshdeskCaseResponse<FreshdeskTacCaseResponseDto> response = updateFreshdeskTacCase(tacCaseDisplayId, updateRequest);

        FreshdeskTacCaseResponseDto responseData = response.getData();
        TacCaseResponseDto tacCaseResponseDto = genericModelMapper.map(responseData, TacCaseResponseDto.class);
        tacCaseResponseDto.setId(freshdeskTicketResponseDto.getId());
        tacCaseResponseDto.setSubject(freshdeskTicketResponseDto.getSubject());
        tacCaseResponseDto.setCaseStatus(status);
        tacCaseResponseDto.setCasePriority(priority);
        return tacCaseResponseDto;
    }


    //fixme: Find All needs Contract for Paging Results
    @Override
    public List<TacCaseResponseDto> findAll() {

        return List.of();
    }

    @Override
    public Optional<TacCaseResponseDto> findById(Long id) {
        TacCaseResponseDto tacCaseResponseDto = findFreshdeskTacCaseByTicketId(id);
        tacCaseResponseDto.setId(id);

        FreshdeskTicketResponseDto ticketResponseDto = findFreshdeskTicketById(id);
        assert ticketResponseDto != null;

        tacCaseResponseDto.setSubject(ticketResponseDto.getSubject());
        tacCaseResponseDto.setCaseStatus(CaseStatus.valueOf(ticketResponseDto.getStatusForTickets().name()));
        tacCaseResponseDto.setCasePriority(CasePriorityEnum.valueOf(ticketResponseDto.getPriorityForTickets().name()));

        return Optional.of(tacCaseResponseDto);
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
     *  fixme: I have already worked on this?????????????
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
        // Retrieve the ticket and its attachments
        FreshdeskTicketResponseDto freshdeskTicketResponseDto = findFreshdeskTicketById(caseId);
        List<FreshdeskAttachment> freshdeskAttachments = freshdeskTicketResponseDto.getAttachments();

        // Handle potential null attachments
        if (freshdeskAttachments == null || freshdeskAttachments.isEmpty()) {
            return List.of(); // Return an empty list if no attachments are found
        }

        // Map attachments to TacCaseAttachmentResponseDto and collect to a list
        return freshdeskAttachments.stream()
                .map(attachment -> TacCaseAttachmentResponseDto.builder()
                        .id(attachment.getId())
                        .name(attachment.getName())
                        .mimeType(attachment.getContentType())
                        .description(attachment.getAttachmentUrl())
                        .build())
                .toList();
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

    //fixme: Should this throw an exception where the other's don't?
    @Override
    public TacCaseNoteResponseDto addNote(Long ticketId, TacCaseNoteUploadDto uploadDto) throws IOException {

        FreshdeskTicketCreateNoteDto dto = FreshdeskTicketCreateNoteDto.builder()
                .body(uploadDto.getText())
                .privateField(false)
                .incoming(true)
                .build();

        FreshdeskTicketNoteResponseDto freshdeskTicketNoteDto = createFreshdeskTicketNote(dto, ticketId);

        return TacCaseNoteResponseDto.builder()
                .id(freshdeskTicketNoteDto.getId())
                .author(uploadDto.getAuthor())
                .tacCaseId(ticketId)
                .date(freshdeskTicketNoteDto.getCreatedAt())
                .build();
    }

    @Override
    public List<TacCaseNoteResponseDto> getAllNotes(Long caseId) {

        //first get all the notes of a ticket
        List<FreshdeskTicketConversationDto> freshdeskTicketConversations = findFreshdeskTicketConversations(caseId);

        return freshdeskTicketConversations.stream()
                .filter(freshdesk -> freshdesk.getSource() == FreshdeskConversationSource.Note)
                .map(freshdesk -> TacCaseNoteResponseDto.builder()
                        .id(freshdesk.getId())
                        .tacCaseId(caseId)
                        .author("FD User ID:"+freshdesk.getUserId().toString())
                        .date(freshdesk.getCreatedAt())
                        .build())
                .toList();
    }

    @Override
    public TacCaseNoteDownloadDto getNote(Long caseId, Long noteId) {
        // Retrieve all the conversations of a ticket
        List<FreshdeskTicketConversationDto> freshdeskTicketConversations = findFreshdeskTicketConversations(caseId);

        // Find and map the note to TacCaseNoteDownloadDto
        return freshdeskTicketConversations.stream()
                .filter(freshdesk -> Objects.equals(freshdesk.getId(), noteId))
                .map(freshdesk -> TacCaseNoteDownloadDto.builder()
                        .id(freshdesk.getId())
                        .tacCaseId(caseId)
                        .author("FD User ID:" + freshdesk.getUserId())
                        .date(freshdesk.getCreatedAt())
                        .text(freshdesk.getBodyText())
                        .build())
                .findFirst() // Get the first matching note
                .orElseThrow(() -> new IllegalArgumentException(
                        "No note found with noteId: " + noteId + " for Case ID: " + caseId));
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

    /*
    TAC Cases
     */
    private FreshdeskCaseResponse<FreshdeskTacCaseResponseDto> updateFreshdeskTacCase(String displayId, FreshdeskTacCaseUpdateRequest updateRequest) {
        String tacCaseSchemaId = schemaService.getSchemaIdByName("TAC Cases");
        return snakeCaseRestClient.put()
                .uri("/custom_objects/schemas/{schema-id}/records/{record-id}", tacCaseSchemaId, displayId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(updateRequest)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

    private FreshdeskTacCaseResponseRecords<FreshdeskTacCaseResponseDto> findFreshdeskTacCaseRecords(Long id) {
        String tacCaseSchemaId = schemaService.getSchemaIdByName("TAC Cases");
        return snakeCaseRestClient.get()
                .uri("/custom_objects/schemas/" + tacCaseSchemaId + "/records?ticket={ticketId}", id)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

    private FreshdeskTicketResponseDto updateTicket(Long id, FreshdeskTicketUpdateDto freshdeskTicketUpdateDto) {
        return fieldPresenseRestClient.put()
                .uri("/tickets/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(freshdeskTicketUpdateDto)
                .retrieve()
                .body(FreshdeskTicketResponseDto.class);
    }

    private FreshdeskCaseResponse<FreshdeskTacCaseResponseDto> updateFreshdeskTacCase(
            FreshdeskDataCreateRequest<FreshdeskTacCaseCreateDto> freshdeskTacCaseCreateRequest) {

        String tacCaseSchemaId = schemaService.getSchemaIdByName("TAC Cases");
        FreshdeskCaseResponse<FreshdeskTacCaseResponseDto> responseTacCase = snakeCaseRestClient.post()
                .uri("/custom_objects/schemas/{schemaId}/records", tacCaseSchemaId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(freshdeskTacCaseCreateRequest)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
        assert responseTacCase != null;
        return responseTacCase;
    }



    /*
    RMA Cases
     */
    //fixme: why isn't this used?
    private static FreshdeskCaseResponse<FreshdeskRmaCaseResponseDto> createFreshdeskRmaCase(String rmaCaseSchemaId,
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


    private FreshdeskTicketCreateDto buildCreateTicket(TacCaseCreateDto tacCaseDto, String responderId) {

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

    private FreshdeskTicketResponseDto createFreshdeskTicket(FreshdeskTicketCreateDto dto) {
        return snakeCaseRestClient.post()
                .uri("/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .body(dto)
                .retrieve()
                .body(FreshdeskTicketResponseDto.class);
    }

    private FreshdeskTicketNoteResponseDto createFreshdeskTicketNote(FreshdeskTicketCreateNoteDto dto, Long id) {
        return snakeCaseRestClient.post()
                .uri("/tickets/{id}/notes", id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(dto)
                .retrieve()
                .body(FreshdeskTicketNoteResponseDto.class);
    }

    private List<FreshdeskTicketConversationDto> findFreshdeskTicketConversations(Long id) {

        return snakeCaseRestClient.get()
                .uri("/tickets/{id}/conversations", id)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });

    }

    private TacCaseResponseDto findFreshdeskTacCaseByTicketId(Long id) {
        FreshdeskTacCaseResponseRecords<FreshdeskTacCaseResponseDto> freshdeskTacCaseRecords = findFreshdeskTacCaseRecords(id);
        Optional<FreshdeskCaseResponse<FreshdeskTacCaseResponseDto>> record = freshdeskTacCaseRecords.getRecords().stream().findFirst();
        FreshdeskTacCaseResponseDto freshdeskTacCaseResponseDto = record.map(FreshdeskCaseResponse::getData).orElse(null);
        return genericModelMapper.map(freshdeskTacCaseResponseDto, TacCaseResponseDto.class);
    }

    private FreshdeskTicketResponseDto findFreshdeskTicketById(Long id) {
        return snakeCaseRestClient.get()
                .uri("/tickets/{id}", id)
                .retrieve()
                .body(FreshdeskTicketResponseDto.class);
    }

    //fixme: look into this one
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
