package com.beaconstrategists.freshdeskapiclient.dtos;

import com.beaconstrategists.freshdeskapiclient.model.Source;
import com.beaconstrategists.freshdeskapiclient.model.Status;
import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;
import java.util.List;

@Value
@Builder
public class TicketResponseDto {
    public List<String> ccEmails;
    public List<String> fwdEmails;
    public List<String> replyCcEmails;
    public List<String> ticketCcEmails;
    public Boolean frEscalated;
    public Boolean spam;
    public Long emailConfigId;
    public Long groupId;
    public Long requesterId;
    public Long responderId;
    public Source source;
    public Long companyId;
    public Status status;
    public String subject;
    public String associationType;
    public String supportEmail;
    public String toEmails;
    public Long productId;
    public Long id;
    public String type;
    public OffsetDateTime dueBy;
    public OffsetDateTime frDueBy;
    public Boolean isEscalated;
    public String description;
    public String descriptionText;
    public OffsetDateTime createdAt;
    public OffsetDateTime updatedAt;
    public List<String> tags;
    public List<TicketAttachmentResponseDto> attachments;
    public String sourceAdditionalInfo;
    public OffsetDateTime nrDueBy;
    public Boolean nrEscalated;
}
