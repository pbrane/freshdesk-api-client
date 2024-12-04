package com.beaconstrategists.freshdeskapiclient.dtos;

import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;

@Value
@Builder
public class TicketAttachmentResponseDto {
     Long id;
     String contentType;
     Long size;
     String name;
     String attachmentUrl;
     OffsetDateTime createdAt;
     OffsetDateTime updatedAt;
}
