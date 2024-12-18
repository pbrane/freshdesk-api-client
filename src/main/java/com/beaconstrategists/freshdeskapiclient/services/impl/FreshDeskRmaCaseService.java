package com.beaconstrategists.freshdeskapiclient.services.impl;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import com.beaconstrategists.freshdeskapiclient.dtos.*;
import com.beaconstrategists.freshdeskapiclient.mappers.FieldPresenceModelMapper;
import com.beaconstrategists.freshdeskapiclient.mappers.GenericModelMapper;
import com.beaconstrategists.freshdeskapiclient.services.CompanyService;
import com.beaconstrategists.freshdeskapiclient.services.SchemaService;
import com.beaconstrategists.taccaseapiservice.controllers.dto.*;
import com.beaconstrategists.taccaseapiservice.model.CaseStatus;
import com.beaconstrategists.taccaseapiservice.services.RmaCaseService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class FreshDeskRmaCaseService implements RmaCaseService {

    private final RestClient snakeCaseRestClient;
    private final RestClient fieldPresenseRestClient;

    private final CompanyService companyService;

    private final GenericModelMapper genericModelMapper;

    @Value("${FD_CUSTOMER_NAME:Microsoft}")
    private String companyName;

    @Value("${FD_DEFAULT_RESPONDER_ID:3043029172572}")
    private String defaultResponderId;

    @Value("${FD_RMA_CASE_ID_PREFIX:_3-}")
    private String rmaCaseIdPrefix;

    private final SchemaService schemaService;

    public FreshDeskRmaCaseService(@Qualifier("snakeCaseRestClient") RestClient snakeCaseRestClient,
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

    @Override
    public RmaCaseResponseDto create(RmaCaseCreateDto rmaCaseCreateDto) {

        //Find the TAC Case
        Long id = rmaCaseCreateDto.getTacCaseId();
        String tacCaseSchemaId = schemaService.getSchemaIdByName("TAC Cases");
        FreshdeskTacCaseResponseRecords<FreshdeskTacCaseResponseDto> freshdeskTacCaseResponseRecords = findFreshdeskTacCaseRecords(id, tacCaseSchemaId);
        assert freshdeskTacCaseResponseRecords != null;

        Optional<FreshdeskCaseResponse<FreshdeskTacCaseResponseDto>> record = freshdeskTacCaseResponseRecords.getRecords().stream().findFirst();
        FreshdeskTacCaseResponseDto freshdeskTacCaseResponseDto = record.map(FreshdeskCaseResponse::getData).orElse(null);
        //get the record's identifier, this is what we need to update the record
        String tacCaseDisplayId = record.get().getDisplayId();
        Long ticketId = freshdeskTacCaseResponseDto.getTicket();
        String rmaKey = freshdeskTacCaseResponseDto.getKey();

        //Create the RMA Case referring to the TAC Case
        FreshdeskRmaCaseCreateDto freshdeskRmaCaseCreateDto = genericModelMapper.map(rmaCaseCreateDto, FreshdeskRmaCaseCreateDto.class);
        Random random = new Random();
        char[] alphabet = {'m','o','l','e','x','m','s','f','t','2','0','2','5'};
        int size = 4;
        String randomKey = NanoIdUtils.randomNanoId(random, alphabet, size);
        freshdeskRmaCaseCreateDto.setKey(rmaKey+"; RMA: "+ randomKey);
        freshdeskRmaCaseCreateDto.setTacCase(tacCaseDisplayId);
        FreshdeskDataCreateRequest<FreshdeskRmaCaseCreateDto> freshdeskRmaCreateRequest = new FreshdeskDataCreateRequest<>(freshdeskRmaCaseCreateDto);
        String rmaCaseSchemaId = schemaService.getSchemaIdByName("RMA Cases");

        FreshdeskCaseResponse<FreshdeskRmaCaseResponseDto> createRmaCaseResponse = snakeCaseRestClient.post()
                .uri("/custom_objects/schemas/{schemaId}/records", rmaCaseSchemaId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(freshdeskRmaCreateRequest)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
        assert createRmaCaseResponse != null;

        Long rmaId = parseRmaId(createRmaCaseResponse.getDisplayId());
        RmaCaseResponseDto rmaCaseResponseDto = genericModelMapper.map(createRmaCaseResponse.getData(), RmaCaseResponseDto.class);
        rmaCaseResponseDto.setId(rmaId);
        rmaCaseResponseDto.setTacCaseId(ticketId);

        //fixme: to do this right, we have to to an update to set this field
//        responseDto.setCaseNumber(createRmaCaseResponse.getDisplayId());

        return rmaCaseResponseDto;
    }

    @Override
    public RmaCaseResponseDto update(Long id, RmaCaseUpdateDto rmaCaseUpdateDto) {

        //fixme: need to get the TAC Case for this RMA Case in order to return the tacCaseId in the Response
        //fixme: so, go ahead and fetch the RMA Case to get the display_id of the TAC Case, then fetch
        //fixme: the TAC Case to get the TicketID
        //
        //fixme: a better solution just might be to store those display_ids in the caseNumber (shrug)


        //find the RMA
        //the FD ID is the display ID which is the RMA Prefix + the ID as a String
        String rmaDisplayId = rmaCaseIdPrefix+id;
        String rmaCaseSchemaId = schemaService.getSchemaIdByName("RMA Cases");

        //First, get the existing record
        //"/api/v2/custom_objects/schemas/{schema-id}/records/{record-id}"
        FreshdeskCaseResponse<FreshdeskRmaCaseResponseDto> freshdeskRmaCaseResponse = snakeCaseRestClient.get()
                .uri("/custom_objects/schemas/{schema-id}/records/{record-id}", rmaCaseSchemaId, rmaDisplayId)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
        assert freshdeskRmaCaseResponse != null;

        FreshdeskRmaCaseResponseDto freshdeskRmaCaseResponseDto = freshdeskRmaCaseResponse.getData();

        //get the TacCase to get the Ticket ID to save in the RMA Update Response as tacCaseId
        String tacCaseDisplayId = freshdeskRmaCaseResponseDto.getTacCase();
        assert tacCaseDisplayId != null;
        String tacCaseSchemaId = schemaService.getSchemaIdByName("TAC Cases");
        FreshdeskCaseResponse<FreshdeskTacCaseResponseDto> freshdeskTacCaseResponse = snakeCaseRestClient.get()
                .uri("/custom_objects/schemas/{schema-id}/records/{record-id}", tacCaseSchemaId, tacCaseDisplayId)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
        assert freshdeskTacCaseResponse != null;
        Long tacCaseId = freshdeskTacCaseResponse.getData().getTicket();

        FreshdeskRmaCaseUpdateDto freshdeskRmaCaseUpdateDto = genericModelMapper.
                map(freshdeskRmaCaseResponseDto, FreshdeskRmaCaseUpdateDto.class);

        //Map the changes from the update request to the existing data
        FieldPresenceModelMapper fieldPresenceModelMapper = new FieldPresenceModelMapper();
        fieldPresenceModelMapper.map(rmaCaseUpdateDto, freshdeskRmaCaseUpdateDto);

        //Send the update
        FreshdeskRmaCaseUpdateRequest updateRequest = new FreshdeskRmaCaseUpdateRequest(freshdeskRmaCaseUpdateDto);
        updateRequest.setVersion(freshdeskRmaCaseResponse.getVersion());
        updateRequest.setDisplayId(freshdeskRmaCaseResponse.getDisplayId());

        freshdeskRmaCaseResponse = snakeCaseRestClient.put()
                .uri("/custom_objects/schemas/{schema-id}/records/{record-id}", rmaCaseSchemaId, rmaDisplayId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(updateRequest)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
        assert freshdeskRmaCaseResponse != null;


        freshdeskRmaCaseResponseDto = freshdeskRmaCaseResponse.getData();
/*
        Note:
        This genericModelMapper returns an instance of FreshdeskRmaCaseResponseDto
        Really weird but fortunately, it works, the extra few fields added to the child class
        are not serialized in the response from the controller
*/
        RmaCaseResponseDto rmaCaseResponseDto = genericModelMapper.map(freshdeskRmaCaseResponseDto, RmaCaseResponseDto.class);
        Long rmaId = parseRmaId(freshdeskRmaCaseResponse.getDisplayId());
        rmaCaseResponseDto.setId(rmaId);
        rmaCaseResponseDto.setTacCaseId(tacCaseId); //tricky part

        return rmaCaseResponseDto;
    }


    @Override
    public Optional<RmaCaseResponseDto> findById(Long id) {
        return Optional.empty();
    }


    @Override
    public void delete(Long id) {

    }

    @Override
    public List<RmaCaseResponseDto> listRmaCases(OffsetDateTime caseCreateDateFrom, OffsetDateTime caseCreateDateTo, OffsetDateTime caseCreateDateSince, List<CaseStatus> caseStatus, String logic) {
        return List.of();
    }

    @Override
    public List<RmaCaseResponseDto> findAll() {
        return List.of();
    }

    @Override
    public boolean exists(Long id) {
        return false;
    }

    @Override
    public RmaCaseAttachmentResponseDto addAttachment(Long caseId, RmaCaseAttachmentUploadDto uploadDto) throws IOException {
        return null;
    }

    @Override
    public List<RmaCaseAttachmentResponseDto> getAllAttachments(Long caseId) {
        return List.of();
    }

    @Override
    public RmaCaseAttachmentResponseDto getAttachment(Long caseId, Long attachmentId) {
        return null;
    }

    @Override
    public RmaCaseAttachmentDownloadDto getAttachmentDownload(Long caseId, Long attachmentId) {
        return null;
    }

    @Override
    public void deleteAttachment(Long caseId, Long attachmentId) {

    }

    @Override
    public void deleteAllAttachments(Long caseId) {

    }

    @Override
    public RmaCaseNoteResponseDto addNote(Long caseId, RmaCaseNoteUploadDto uploadDto) throws IOException {
        return null;
    }

    @Override
    public List<RmaCaseNoteResponseDto> getAllNotes(Long caseId) {
        return List.of();
    }

    @Override
    public RmaCaseNoteDownloadDto getNote(Long caseId, Long noteId) {
        return null;
    }

    @Override
    public void deleteNote(Long caseId, Long noteId) {

    }

    @Override
    public void deleteAllNotes(Long caseId) {

    }


    /*
    Helper Methods
     */

    private Long parseRmaId(String displayId) {
        Long rmaId;
        if (displayId.contains(rmaCaseIdPrefix)) {
            String value = displayId.substring(displayId.indexOf(rmaCaseIdPrefix) + rmaCaseIdPrefix.length());
            rmaId = Long.valueOf(value);
            System.out.println("Extracted Integer: " + rmaId);
        } else {
            System.out.println("Prefix not found");
            throw new IllegalStateException("Prefix: { "+rmaCaseIdPrefix+" } not found.");
        }

        return rmaId;
    }

    private FreshdeskTacCaseResponseRecords<FreshdeskTacCaseResponseDto> findFreshdeskTacCaseRecords(Long id, String tacCaseSchemaId) {
        return snakeCaseRestClient.get()
                .uri("/custom_objects/schemas/" + tacCaseSchemaId + "/records?ticket={ticketId}", id)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

}
