package com.beaconstrategists.freshdeskapiclient.services;

import com.beaconstrategists.freshdeskapiclient.model.Ticket;

import java.util.List;

public interface TicketService {

    List<Ticket> getTickets();
    Ticket createTicket(Ticket ticket);
    Ticket getTicket(Integer id);
    Ticket updateTicket(Integer id, Ticket ticket);
    void deleteTicket(Integer id);
    Ticket patchTicket(Ticket ticket);
}
