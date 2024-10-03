package com.beaconstrategists.freshdeskapiclient.controllers;

import com.beaconstrategists.freshdeskapiclient.model.Ticket;
import com.beaconstrategists.freshdeskapiclient.services.TicketService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api")
public class TicketController {


    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
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
