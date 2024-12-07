package com.beaconstrategists.freshdeskapiclient.controllers;

import com.beaconstrategists.freshdeskapiclient.model.Ticket;
import com.beaconstrategists.freshdeskapiclient.services.TicketService;
import com.beaconstrategists.taccaseapiservice.controllers.dto.TacCaseCreateDto;
import com.beaconstrategists.taccaseapiservice.controllers.dto.TacCaseResponseDto;
import com.beaconstrategists.taccaseapiservice.services.TacCaseService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/api")
public class TicketController {


    private final TicketService ticketService;
    private final TacCaseService tacCaseService;

    public TicketController(TicketService ticketService, TacCaseService tacCaseService) {
        this.ticketService = ticketService;
        this.tacCaseService = tacCaseService;
    }

    @PostMapping("/tacCases")
    @ResponseStatus(HttpStatus.ACCEPTED)
    TacCaseResponseDto createTacCase(@RequestBody TacCaseCreateDto dto) {
        return tacCaseService.create(dto);
    }

    @GetMapping("/tacCases/{id}")
    Optional<TacCaseResponseDto> getTacCase(@PathVariable Long id) {
        return tacCaseService.findById(id);
    }

    @PostMapping("/tickets")
    @ResponseStatus(HttpStatus.CREATED)
    Ticket createTicket(@RequestBody Ticket ticket) {
        return ticketService.createTicket(ticket);
    }

    @GetMapping("/tickets")
    List<Ticket> getTickets() {
        return ticketService.getTickets();
    }

    @GetMapping("/tickets/{id}")
    Ticket getTicket(@PathVariable Integer id) {
        return ticketService.getTicket(id);
    }

    @PutMapping("/tickets/{id}")
    Ticket updateTicket(@PathVariable Integer id, @RequestBody Ticket ticket) {
        return ticketService.updateTicket(id, ticket);
    }

    @DeleteMapping("/tickets/{id}")
    void deleteTicket(@PathVariable Integer id) {
        ticketService.deleteTicket(id);
    }

}
