package com.beaconstrategists.freshdeskapiclient.services.impl;

import com.beaconstrategists.freshdeskapiclient.model.Priority;
import com.beaconstrategists.freshdeskapiclient.model.Status;
import com.beaconstrategists.freshdeskapiclient.model.Ticket;
import com.beaconstrategists.freshdeskapiclient.services.CompanyService;
import com.beaconstrategists.freshdeskapiclient.services.SchemaService;
import com.beaconstrategists.freshdeskapiclient.services.TicketService;
import com.beaconstrategists.taccaseapiservice.controllers.dto.TacCaseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class FreshdeskTicketService implements TicketService {

    private final RestClient restClient;
    private final SchemaService schemaService;
    private final CompanyService companyService;

    @Value("${FD_CUSTOMER_NAME:Microsoft}")
    private String companyName;

    public FreshdeskTicketService(RestClient restClient, SchemaService schemaService, CompanyService companyService) {
        this.restClient = restClient;
        this.schemaService = schemaService;
        this.companyService = companyService;
    }

    @Override
    public List<Ticket> getTickets() {

        return restClient.get()
                .uri("/tickets")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

    @Override
    public Ticket createTicket(Ticket ticket) {
        return restClient.post()
                .uri("/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .body(ticket)
                .retrieve()
                .body(Ticket.class);
    }

    public TacCaseDto createTacCase(TacCaseDto tacCaseDto) {

        String companyId = companyService.getCompanyIdByName(companyName);

        Ticket ticket = Ticket.builder().descriptionText(tacCaseDto.getProblemDescription())
                .email(tacCaseDto.getContactEmail())
                .priority(Priority.valueOf(tacCaseDto.getCasePriority().toString())) //Updated Case Priority to Match Freshdesk Default Priorities
                .descriptionText(tacCaseDto.getProblemDescription())
                .companyId(companyId)
                .status(Status.valueOf(tacCaseDto.getCaseStatus().toString()))
                .subject(tacCaseDto.getSubject())
                .build();

        Ticket body = restClient.post()
                .uri("/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .body(ticket)
                .retrieve()
                .body(Ticket.class);

        String tacCaseSchemaId = schemaService.getSchemaIdByName("TAC Cases");
        TacCaseDto tacCase = restClient.post()
                .uri("custom_objects/schemas/"+tacCaseSchemaId+"/records")
                .contentType(MediaType.APPLICATION_JSON)
                .body(tacCaseDto)
                .retrieve()
                .body(TacCaseDto.class);

        return null;
    }

    @Override
    public Ticket getTicket(Integer id) {
        return restClient
                .get()
                .uri("/tickets/{id}", id)
                .accept(MediaType.APPLICATION_JSON).retrieve()
                .body(Ticket.class);
    }

    @Override
    public Ticket updateTicket(Integer id, Ticket ticket) {
        return restClient.put()
                .uri("/tickets/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ticket)
                .retrieve()
                .body(Ticket.class);
    }

    @Override
    public void deleteTicket(Integer id) {
        restClient.delete()
                .uri("/tickets/{id}", id)
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public Ticket patchTicket(Ticket ticket) {
        return null;
    }
}
