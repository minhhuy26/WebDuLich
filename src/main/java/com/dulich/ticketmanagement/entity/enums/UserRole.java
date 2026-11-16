package com.dulich.ticketmanagement.entity.enums;

public enum UserRole {
    ROLE_TRAVELER("Khách hàng / Du khách"),
    ROLE_AGENT("Đại lý du lịch"),
    ROLE_OPERATOR("Nhân viên điều hành / Vận hành"),
    ROLE_ADMIN("Quản trị viên hệ thống");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
