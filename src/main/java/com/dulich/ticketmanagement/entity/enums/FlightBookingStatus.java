package com.dulich.ticketmanagement.entity.enums;

public enum FlightBookingStatus {
    HELD("Đang giữ chỗ (15 phút)"),
    CONFIRMED("Đã thanh toán / Xác nhận"),
    ISSUED("Đã xuất vé điện tử"),
    CANCELLED("Đã hủy vé"),
    CHANGED("Đã đổi chuyến"),
    EXPIRED("Hết hạn giữ chỗ");

    private final String displayName;

    FlightBookingStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
