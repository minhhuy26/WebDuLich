package com.dulich.ticketmanagement.entity.enums;

public enum PaymentMethod {
    VNPAY("Cổng thanh toán VNPAY-QR"),
    BANK_TRANSFER("Chuyển khoản ngân hàng"),
    CREDIT_CARD("Thẻ tín dụng / Thẻ ghi nợ quốc tế"),
    CASH("Tiền mặt tại phòng vé");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
