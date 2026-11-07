package com.dulich.ticketmanagement.entity.enums;

public enum PaymentStatus {
    PENDING("Chờ thanh toán"),
    SUCCESS("Thành công"),
    FAILED("Thất bại"),
    REFUNDED("Đã hoàn tiền");

    private final String displayName;

    PaymentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
