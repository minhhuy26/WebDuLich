package com.dulich.ticketmanagement.entity.enums;

public enum TicketStatus {
    AVAILABLE("Còn trống"),
    HELD("Đang giữ chỗ"),
    ISSUED("Đã xuất vé"),
    CANCELLED("Đã hủy");

    private final String displayName;

    TicketStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
