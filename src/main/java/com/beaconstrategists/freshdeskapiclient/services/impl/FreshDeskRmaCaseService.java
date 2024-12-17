package com.beaconstrategists.freshdeskapiclient.services.impl;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import com.beaconstrategists.freshdeskapiclient.dtos.*;
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

        Long rmaId = parseRmaId(createRmaCaseResponse);

        RmaCaseResponseDto responseDto = genericModelMapper.map(createRmaCaseResponse.getData(), RmaCaseResponseDto.class);
        responseDto.setId(rmaId);
        responseDto.setTacCaseId(ticketId);

        //fixme: to do this right, we have to to an update to set this field
//        responseDto.setCaseNumber(createRmaCaseResponse.getDisplayId());

        return responseDto;
    }

    @Override
    public Optional<RmaCaseResponseDto> findById(Long id) {
        return Optional.empty();
    }

    @Override
    public RmaCaseResponseDto update(Long id, RmaCaseUpdateDto rmaCaseUpdateDto) {
        return null;
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

    private Long parseRmaId(FreshdeskCaseResponse<FreshdeskRmaCaseResponseDto> createRmaCaseResponse) {
        Long rmaId;
        String rmaCaseDisplayId = createRmaCaseResponse.getDisplayId();
        if (rmaCaseDisplayId.contains(rmaCaseIdPrefix)) {
            String value = rmaCaseDisplayId.substring(rmaCaseDisplayId.indexOf(rmaCaseIdPrefix) + rmaCaseIdPrefix.length());
            rmaId = Long.valueOf(value);
            System.out.println("Extracted Integer: " + rmaId);
        } else {
            System.out.println("Prefix not found");
            throw new IllegalStateException("Prefix: { "+rmaCaseIdPrefix+" } not found.");
        }
        return rmaId;
    }

    private FreshdeskTacCaseResponseRecords<FreshdeskTacCaseResponseDto> findFreshdeskTacCaseRecords(Long id, String tacCaseSchemaId) {
        FreshdeskTacCaseResponseRecords<FreshdeskTacCaseResponseDto> freshdeskTacCaseResponseRecords = snakeCaseRestClient.get()
                .uri("/custom_objects/schemas/" + tacCaseSchemaId + "/records?ticket={ticketId}", id)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
        return freshdeskTacCaseResponseRecords;
    }

}
