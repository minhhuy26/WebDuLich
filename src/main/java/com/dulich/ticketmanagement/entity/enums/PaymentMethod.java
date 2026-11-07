package com.dulich.ticketmanagement.entity.enums;

public enum PaymentMethod {
    CASH("Tiền mặt"),
    BANK_TRANSFER("Chuyển khoản"),
    CREDIT_CARD("Thẻ tín dụng / Ghi nợ"),
    VN_PAY("Cổng VNPAY");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
