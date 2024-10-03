package com.beaconstrategists.freshdeskapiclient.model;

public enum Source {
    Email(1),
    Portal(2),
    Phone(3),
    Chat(7),
    Feedback_Widget(9),
    Outbound_Email(10);

    Source(int i) {
    }
}
