package com.beaconstrategists.freshdeskapiclient.dtos;

import com.beaconstrategists.taccaseapiservice.dtos.TacCaseAttachmentDownloadDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FreshDeskTacCaseNoteResponseDto {

    private String body;
    private String bodyText;
    private Long id;
    private Boolean incoming;
    private Boolean privateField; //fixme: this may be a problem (private is a keyword)
    private Long userId;
    private String supportEmail;
    private Long ticketId;
    private List<String> toEmails;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private List<TacCaseAttachmentDownloadDto> attachments; //fixme: this isn't the write structure


}
