package com.luxehaven.booking.model;

public enum RoomType {
    STANDARD("Tiêu chuẩn"),
    DELUXE("Cao cấp"),
    SUITE("Hạng sang");

    private final String displayName;

    RoomType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
