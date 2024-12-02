package com.beaconstrategists.freshdeskapiclient.services.impl;

import com.beaconstrategists.freshdeskapiclient.dtos.TicketAttachmentUploadDto;
import com.beaconstrategists.freshdeskapiclient.dtos.TicketCreateUpdateDto;
import com.beaconstrategists.freshdeskapiclient.dtos.TicketResponseDto;
import com.beaconstrategists.freshdeskapiclient.model.Priority;
import com.beaconstrategists.freshdeskapiclient.model.Source;
import com.beaconstrategists.freshdeskapiclient.model.Status;
import com.beaconstrategists.freshdeskapiclient.services.CompanyService;
import com.beaconstrategists.freshdeskapiclient.services.SchemaService;
import com.beaconstrategists.taccaseapiservice.controllers.dto.TacCaseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class FreshDeskCaseService {

    private final RestClient restClient;
    private final SchemaService schemaService;
    private final CompanyService companyService;

    @Value("${FD_CUSTOMER_NAME:Microsoft}")
    private String companyName;

    public FreshDeskCaseService(RestClient restClient, SchemaService schemaService, CompanyService companyService) {
        this.restClient = restClient;
        this.schemaService = schemaService;
        this.companyService = companyService;
    }

    public TacCaseDto save(TacCaseDto tacCaseDto) {

        //Map TacCase to Ticket
        TicketCreateUpdateDto ticket = buildTicket(tacCaseDto);
        TicketResponseDto ticketResponse = null;

        try {
            //is this a new ticket... following the JPA save method, if the ID is null, it's new
            if (ticket.getId() == null) {
                ticketResponse = restClient.post()
                        .uri("/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(ticket)
                        .retrieve()
                        .body(TicketResponseDto.class); // Automatically maps to the response type
            } else {
                ticketResponse = restClient.put()
                        .uri("/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(ticket)
                        .retrieve()
                        .body(TicketResponseDto.class);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error creating/updating ticket: " + e.getMessage(), e);
        }

        assert ticketResponse != null;
        Long ticketId = ticketResponse.getId();

        //Create or update tacCase
        //is there a tac case?
        Long caseId = null;

        String tacCaseSchemaId = schemaService.getSchemaIdByName("TAC Cases");



        TacCaseDto tacCase = restClient.post()
                .uri("custom_objects/schemas/"+tacCaseSchemaId+"/records")
                .contentType(MediaType.APPLICATION_JSON)
                .body(tacCaseDto)
                .retrieve()
                .body(TacCaseDto.class);






        return null;
    }

    private static TicketCreateUpdateDto buildTicket(TacCaseDto tacCaseDto) {
        return TicketCreateUpdateDto.builder()
                .email(tacCaseDto.getContactEmail())
                .subject(tacCaseDto.getSubject())
                .responderId(Long.getLong("1235")) //fixme
                .type("Problem")
                .source(Source.Email)
                .status(Status.valueOf(tacCaseDto.getCaseStatus().toString())) //fixme: verify this
                .priority(Priority.valueOf(tacCaseDto.getCasePriority().toString()))
                .description(tacCaseDto.getProblemDescription())
                .id(tacCaseDto.getId())
                .build();
    }

    public TicketResponseDto createTicket(TicketCreateUpdateDto ticketDto) {
        try {
            TicketResponseDto body = restClient.post()
                    .uri("/tickets")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(ticketDto)
                    .retrieve()
                    .body(TicketResponseDto.class);

            return body; // Automatically maps to the response type

        } catch (Exception e) {
            throw new RuntimeException("Error creating ticket: " + e.getMessage(), e);
        }
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
