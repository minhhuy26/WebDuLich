package com.dulich.ticketmanagement.entity.enums;

public enum TicketType {
    FLIGHT("Vé máy bay"),
    TRAIN("Vé tàu hỏa");

    private final String displayName;

    TicketType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
