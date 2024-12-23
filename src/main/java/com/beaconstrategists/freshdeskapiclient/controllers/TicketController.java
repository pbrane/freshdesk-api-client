package com.beaconstrategists.freshdeskapiclient.controllers;

import com.beaconstrategists.taccaseapiservice.dtos.*;
import com.beaconstrategists.taccaseapiservice.model.CaseStatus;
import com.beaconstrategists.taccaseapiservice.services.RmaCaseService;
import com.beaconstrategists.taccaseapiservice.services.TacCaseService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.OffsetDateTime;
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

    @GetMapping(path = "/tacCases")
    public ResponseEntity<List<TacCaseResponseDto>> listAllTacCases(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime caseCreateDateFrom,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime caseCreateDateTo,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime caseCreateDateSince,

            @RequestParam(required = false)
            List<CaseStatus> caseStatus,

            @RequestParam(required = false, defaultValue = "AND")
            String logic
    ) {
        List<TacCaseResponseDto> tacCases = tacCaseService.listTacCases(
                caseCreateDateFrom,
                caseCreateDateTo,
                caseCreateDateSince,
                caseStatus,
                logic
        );
        return new ResponseEntity<>(tacCases, HttpStatus.OK);
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

    @PostMapping("/tacCases/{id}/notes")
    public ResponseEntity<TacCaseNoteResponseDto> uploadNote(
            @PathVariable Long id,
            @Valid @RequestBody TacCaseNoteUploadDto uploadDto) throws IOException {
        TacCaseNoteResponseDto responseDto = tacCaseService.addNote(id, uploadDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @GetMapping("/tacCases/{id}/notes")
    public ResponseEntity<List<TacCaseNoteResponseDto>> getAllNotes(@PathVariable Long id) {
        List<TacCaseNoteResponseDto> notes = tacCaseService.getAllNotes(id);
        return new ResponseEntity<>(notes, HttpStatus.OK);
    }

    @GetMapping("/tacCases/{caseId}/notes/{noteId}")
    public ResponseEntity<TacCaseNoteDownloadDto> getNote(
            @PathVariable Long caseId, @PathVariable Long noteId) {
        TacCaseNoteDownloadDto dto = tacCaseService.getNote(caseId, noteId);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @PostMapping("/tacCases/{id}/attachments")
    public ResponseEntity<TacCaseAttachmentResponseDto> uploadAttachment(
            @PathVariable Long id,
            @Valid @ModelAttribute TacCaseAttachmentUploadDto uploadDto) {

        //fixme: these responses may not be valid
        try {
            TacCaseAttachmentResponseDto responseDto = tacCaseService.addAttachment(id, uploadDto);
            return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            // Handle validation errors
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (IOException e) {
            // Handle file processing exceptions
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/tacCases/{id}/attachments")
    public ResponseEntity<List<TacCaseAttachmentResponseDto>> getAllAttachments(@PathVariable Long id) {
        List<TacCaseAttachmentResponseDto> attachments = tacCaseService.getAllAttachments(id);
        return new ResponseEntity<>(attachments, HttpStatus.OK);
    }

    @GetMapping("/tacCases/{caseId}/attachments/{attachmentId}")
    public ResponseEntity<TacCaseAttachmentResponseDto> getAttachment(@PathVariable Long caseId, @PathVariable Long attachmentId) {
        TacCaseAttachmentResponseDto attachment = tacCaseService.getAttachment(caseId, attachmentId);
        return new ResponseEntity<>(attachment, HttpStatus.OK);
    }

    @PostMapping("/rmaCases")
    @ResponseStatus(HttpStatus.ACCEPTED)
    RmaCaseResponseDto createRmaCase(@RequestBody RmaCaseCreateDto dto) {
        RmaCaseResponseDto rmaCaseResponseDto = rmaCaseService.create(dto);
        return rmaCaseResponseDto;
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
