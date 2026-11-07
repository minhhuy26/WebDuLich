package com.dulich.ticketmanagement.entity.enums;

import java.math.BigDecimal;

public enum FlightSeatClass {
    ECONOMY("Phổ thông (Economy)", new BigDecimal("1.0"), 20),
    PREMIUM("Phổ thông đặc biệt (Premium)", new BigDecimal("1.5"), 30),
    BUSINESS("Thương gia (Business)", new BigDecimal("2.5"), 40);

    private final String displayName;
    private final BigDecimal multiplier;
    private final int freeBaggageKg; // Định mức hành lý miễn cước

    FlightSeatClass(String displayName, BigDecimal multiplier, int freeBaggageKg) {
        this.displayName = displayName;
        this.multiplier = multiplier;
        this.freeBaggageKg = freeBaggageKg;
    }

    public String getDisplayName() {
        return displayName;
    }

    public BigDecimal getMultiplier() {
        return multiplier;
    }

    public int getFreeBaggageKg() {
        return freeBaggageKg;
    }
}
