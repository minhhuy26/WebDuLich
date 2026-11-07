package com.dulich.ticketmanagement.entity.enums;

public enum ConsultationStatus {
    PENDING("Chờ xác nhận"),
    CONFIRMED("Đã xác nhận"),
    COMPLETED("Hoàn thành"),
    CANCELLED("Đã hủy");

    private final String displayName;

    ConsultationStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
