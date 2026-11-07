package com.dulich.ticketmanagement.entity.enums;

public enum UserRole {
    ADMIN("Quản trị viên"),
    NHANVIEN("Nhân viên"),
    KHACH("Khách hàng"),
    STAFF("Nhân viên tư vấn");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
