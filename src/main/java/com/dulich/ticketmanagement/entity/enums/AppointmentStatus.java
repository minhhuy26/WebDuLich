package com.dulich.ticketmanagement.entity.enums;

public enum AppointmentStatus {
    PENDING("Chờ tiếp nhận"),
    CONFIRMED("Đã xác nhận hẹn"),
    COMPLETED("Đã hoàn tất tư vấn"),
    CANCELLED("Đã hủy");

    private final String displayName;

    AppointmentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
