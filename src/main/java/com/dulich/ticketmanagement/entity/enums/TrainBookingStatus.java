package com.dulich.ticketmanagement.entity.enums;

public enum TrainBookingStatus {
    HELD("Đang giữ chỗ (10 phút)"),
    CONFIRMED("Đã thanh toán / Xác nhận"),
    ISSUED("Đã xuất vé tàu điện tử"),
    CANCELLED("Đã hủy vé"),
    EXPIRED("Hết hạn giữ chỗ");

    private final String displayName;

    TrainBookingStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
