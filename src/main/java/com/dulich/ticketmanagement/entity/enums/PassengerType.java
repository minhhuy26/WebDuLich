package com.dulich.ticketmanagement.entity.enums;

public enum PassengerType {
    ADULT("Người lớn"),
    CHILD("Trẻ em"),
    INFANT("Em bé"),
    SENIOR("Người cao tuổi (Giảm 15%)"),
    STUDENT("Học sinh / Sinh viên (Giảm 10%)");

    private final String displayName;

    PassengerType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
