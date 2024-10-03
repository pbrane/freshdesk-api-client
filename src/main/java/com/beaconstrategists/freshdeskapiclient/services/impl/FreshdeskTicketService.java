package com.beaconstrategists.freshdeskapiclient.services.impl;

import com.beaconstrategists.freshdeskapiclient.model.Ticket;
import com.beaconstrategists.freshdeskapiclient.services.TicketService;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class FreshdeskTicketService implements TicketService {

    private final RestClient restClient;

    public FreshdeskTicketService(RestClient restClient) {
        this.restClient = restClient;
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
