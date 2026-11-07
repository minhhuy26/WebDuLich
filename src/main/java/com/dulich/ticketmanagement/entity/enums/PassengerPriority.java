package com.dulich.ticketmanagement.entity.enums;

import java.math.BigDecimal;

public enum PassengerPriority {
    STANDARD("Người lớn tiêu chuẩn", BigDecimal.ZERO, "Không yêu cầu"),
    CHILD_6_10("Trẻ em (6 - 10 tuổi)", new BigDecimal("0.25"), "Ngày sinh / Giấy khai sinh / CCCD"),
    STUDENT("Sinh viên", new BigDecimal("0.10"), "Thẻ sinh viên còn hạn"),
    SENIOR("Người cao tuổi (≥ 60 tuổi)", new BigDecimal("0.15"), "CCCD xác thực độ tuổi");

    private final String displayName;
    private final BigDecimal discountRate; // 0.25 = 25%
    private final String requiredDocNote;

    PassengerPriority(String displayName, BigDecimal discountRate, String requiredDocNote) {
        this.displayName = displayName;
        this.discountRate = discountRate;
        this.requiredDocNote = requiredDocNote;
    }

    public String getDisplayName() {
        return displayName;
    }

    public BigDecimal getDiscountRate() {
        return discountRate;
    }

    public String getRequiredDocNote() {
        return requiredDocNote;
    }
}
