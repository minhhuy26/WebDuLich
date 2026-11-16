package com.dulich.ticketmanagement.entity.enums;

public enum TransportType {
    AIR("Hàng không / Vé máy bay", "Máy bay", "fa-plane"),
    TRAIN("Đường sắt / Vé tàu hỏa", "Tàu hỏa", "fa-train");

    private final String fullName;
    private final String shortName;
    private final String icon;

    TransportType(String fullName, String shortName, String icon) {
        this.fullName = fullName;
        this.shortName = shortName;
        this.icon = icon;
    }

    public String getFullName() {
        return fullName;
    }

    public String getShortName() {
        return shortName;
    }

    public String getIcon() {
        return icon;
    }
}
