package com.luxehaven.booking.model;

public enum RoomStatus {
    AVAILABLE("Còn trống"),
    OCCUPIED("Đang có khách"),
    MAINTENANCE("Đang bảo trì");

    private final String displayName;

    RoomStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
