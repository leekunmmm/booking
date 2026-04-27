package com.luxehaven.booking.model;

public enum PaymentStatus {
    UNPAID("Chưa thanh toán"),
    PENDING_CONFIRMATION("Chờ admin xác nhận"),
    PAID("Đã thanh toán");

    private final String displayName;

    PaymentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
