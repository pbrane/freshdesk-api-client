package com.beaconstrategists.freshdeskapiclient.controllers;

import com.beaconstrategists.taccaseapiservice.dtos.*;
import com.beaconstrategists.taccaseapiservice.services.RmaCaseService;
import com.beaconstrategists.taccaseapiservice.services.TacCaseService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/api/v1")
public class TicketController {


    private final TacCaseService tacCaseService;
    private final RmaCaseService rmaCaseService;

    public TicketController(@Qualifier("FreshdeskTacCaseService") TacCaseService tacCaseService,
                            @Qualifier("FreshdeskRmaCaseService") RmaCaseService rmaCaseService) {
        this.tacCaseService = tacCaseService;
        this.rmaCaseService = rmaCaseService;
    }

    @PostMapping("/rmaCases")
    @ResponseStatus(HttpStatus.ACCEPTED)
    RmaCaseResponseDto createRmaCase(@RequestBody RmaCaseCreateDto dto) {
        RmaCaseResponseDto rmaCaseResponseDto = rmaCaseService.create(dto);
        return rmaCaseResponseDto;
    }

    @PostMapping("/tacCases")
    @ResponseStatus(HttpStatus.ACCEPTED)
    TacCaseResponseDto createTacCase(@RequestBody TacCaseCreateDto dto) {
        return tacCaseService.create(dto);
    }

    @GetMapping("/tacCases/{id}")
    public ResponseEntity<TacCaseResponseDto> getTacCase(@PathVariable Long id) {
        return tacCaseService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/tacCases/{id}")
    TacCaseResponseDto updateTacCase(@PathVariable Long id, @RequestBody TacCaseUpdateDto dto) {
        return tacCaseService.update(id, dto);
    }

    @PutMapping("/rmaCases/{id}")
    RmaCaseResponseDto updateRmaCase(@PathVariable Long id, @RequestBody RmaCaseUpdateDto dto) {
        return rmaCaseService.update(id, dto);
    }

    @GetMapping("/rmaCases/{id}")
    public ResponseEntity<RmaCaseResponseDto> getRmaCase(@PathVariable Long id) {
        return rmaCaseService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/notes")
    public ResponseEntity<TacCaseNoteResponseDto> uploadNote(
            @PathVariable Long id,
            @Valid @RequestBody TacCaseNoteUploadDto uploadDto) throws IOException {
        TacCaseNoteResponseDto responseDto = tacCaseService.addNote(id, uploadDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @GetMapping("/{id}/notes")
    public ResponseEntity<List<TacCaseNoteResponseDto>> getAllNotes(@PathVariable Long id) {
        List<TacCaseNoteResponseDto> notes = tacCaseService.getAllNotes(id);
        return new ResponseEntity<>(notes, HttpStatus.OK);
    }

    @GetMapping("/{caseId}/notes/{noteId}")
    public ResponseEntity<TacCaseNoteDownloadDto> getNote(
            @PathVariable Long caseId, @PathVariable Long noteId) {
        TacCaseNoteDownloadDto dto = tacCaseService.getNote(caseId, noteId);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @GetMapping("/{id}/attachments")
    public ResponseEntity<List<TacCaseAttachmentResponseDto>> getAllAttachments(@PathVariable Long id) {
        List<TacCaseAttachmentResponseDto> attachments = tacCaseService.getAllAttachments(id);
        return new ResponseEntity<>(attachments, HttpStatus.OK);
    }




/*
    @PostMapping("/tickets")
    @ResponseStatus(HttpStatus.CREATED)
    Ticket createTicket(@RequestBody Ticket ticket) {
        return ticketService.createTicket(ticket);
    }
*/

/*
    @GetMapping("/tickets")
    List<Ticket> getTickets() {
        return ticketService.getTickets();
    }
*/

/*
    @GetMapping("/tickets/{id}")
    Ticket getTicket(@PathVariable Integer id) {
        return ticketService.getTicket(id);
    }
*/

/*
    @PutMapping("/tickets/{id}")
    Ticket updateTicket(@PathVariable Integer id, @RequestBody Ticket ticket) {
        return ticketService.updateTicket(id, ticket);
    }
*/

/*
    @DeleteMapping("/tickets/{id}")
    void deleteTicket(@PathVariable Integer id) {
        ticketService.deleteTicket(id);
    }
*/

}
