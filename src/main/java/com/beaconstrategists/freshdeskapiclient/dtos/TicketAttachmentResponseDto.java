package com.beaconstrategists.freshdeskapiclient.dtos;

import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;

@Value
@Builder
public class TicketAttachmentResponseDto {
    public Long id;
    public String contentType;
    public Long size;
    public String name;
    public String attachmentUrl;
    public OffsetDateTime createdAt;
    public OffsetDateTime updatedAt;
}
