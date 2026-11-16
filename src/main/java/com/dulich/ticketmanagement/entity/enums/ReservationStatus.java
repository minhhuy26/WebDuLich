package com.dulich.ticketmanagement.entity.enums;

public enum ReservationStatus {
    CREATED("Đang giữ chỗ"),
    CONFIRMED("Đã thanh toán / Đã xuất vé"),
    CANCELLED("Đã hủy"),
    EXPIRED("Hết hạn");

    private final String displayName;

    ReservationStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
