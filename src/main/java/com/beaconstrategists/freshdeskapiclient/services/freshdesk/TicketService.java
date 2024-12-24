package com.beaconstrategists.freshdeskapiclient.services.freshdesk;

import com.beaconstrategists.freshdeskapiclient.model.freshdesk.Ticket;

import java.util.List;

public interface TicketService {

    //Basic ticket functions
    List<Ticket> getTickets();
    Ticket createTicket(Ticket ticket);
    Ticket getTicket(Integer id);
    Ticket updateTicket(Integer id, Ticket ticket);
    void deleteTicket(Integer id);
    Ticket patchTicket(Ticket ticket);
}
