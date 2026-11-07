package com.dulich.ticketmanagement.entity.enums;

import java.math.BigDecimal;

public enum CoachType {
    SOFT_SEAT("Ghế mềm điều hòa", new BigDecimal("700")),
    SLEEPER_6("Giường nằm khoang 6", new BigDecimal("1000")),
    SLEEPER_4("Giường nằm khoang 4 VIP", new BigDecimal("1300"));

    private final String displayName;
    private final BigDecimal defaultPricePerKm;

    CoachType(String displayName, BigDecimal defaultPricePerKm) {
        this.displayName = displayName;
        this.defaultPricePerKm = defaultPricePerKm;
    }

    public String getDisplayName() {
        return displayName;
    }

    public BigDecimal getDefaultPricePerKm() {
        return defaultPricePerKm;
    }
}
