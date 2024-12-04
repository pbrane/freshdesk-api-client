package com.beaconstrategists.freshdeskapiclient.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Objects;

@Getter
public enum TicketTacTacCaseStatus {
    Open(2),
    Pending(3),
    Resolved(4),
    Closed(5);

    private final int value;

    TicketTacTacCaseStatus(int value) {
        this.value = value;
    }

    public static TicketTacTacCaseStatus fromValue(int value) {
        for (TicketTacTacCaseStatus status : TicketTacTacCaseStatus.values()) {
            if (status.getValue() == value) {
                return status;
            }
        }
        return null;
    }

    public static TicketTacTacCaseStatus fromString(String value) {
        for (TicketTacTacCaseStatus status : TicketTacTacCaseStatus.values()) {
            if (Objects.equals(status.getName(), value))
                return status;
        }
        return null;
    }

    @JsonValue
    private String getName() {
        return switch (this) {
            case Open -> "Open";
            case Resolved -> "Resolved";
            case Closed -> "Closed";
            default -> null;
        };
    }
}
