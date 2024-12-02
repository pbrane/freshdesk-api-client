package com.beaconstrategists.freshdeskapiclient.dtos;

import lombok.Builder;
import lombok.Value;
import org.springframework.web.multipart.MultipartFile;

@Value
@Builder
public class TicketAttachmentUploadDto {
    //    private Long id;
    String name;
    String mimeType;
    String description;
    Float size;
    MultipartFile file; // Include binary data

}
