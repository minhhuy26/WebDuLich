package com.dulich.ticketmanagement.entity.enums;

public enum PassengerType {
    ADULT("Người lớn"),
    CHILD("Trẻ em"),
    INFANT("Em bé");

    private final String displayName;

    PassengerType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
