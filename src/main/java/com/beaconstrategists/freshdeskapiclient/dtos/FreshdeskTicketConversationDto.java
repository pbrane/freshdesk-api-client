package com.beaconstrategists.freshdeskapiclient.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FreshdeskTicketConversationDto {

    private String body;
    private String bodyText;
    private Long id;
    private boolean incoming;
    private boolean isPrivate;
    private Long userId;
    private String supportEmail;
    private Integer source;
    private Integer category;
    private List<String> toEmails;
    private String fromEmail;
    private List<String> ccEmails;
    private List<String> bccEmails;
    private Integer emailFailureCount;
    private Integer outgoingFailures;
    private Long threadId;
    private String threadMessageId;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private ZonedDateTime lastEditedAt;
    private Long lastEditedUserId;
    private List<Attachment> attachments;
    private Long automationId;
    private Long automationTypeId;
    private boolean autoResponse;
    private Long ticketId;
    private String threadingType;
    private DeliveryDetails deliveryDetails;
    private String sourceAdditionalInfo;

    // Getters and Setters

    public static class Attachment {
        private Long id;
        private String name;
        private String contentType;
        private Integer size;
        private ZonedDateTime createdAt;
        private ZonedDateTime updatedAt;
        private String attachmentUrl;

        // Getters and Setters
    }

    public static class DeliveryDetails {
        private List<String> failedEmails;
        private List<String> pendingEmails;

    }
}
