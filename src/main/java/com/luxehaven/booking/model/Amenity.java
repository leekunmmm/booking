package com.luxehaven.booking.model;

public enum Amenity {
    WIFI("Wi-Fi miễn phí", "bi-wifi"),
    AC("Điều hoà", "bi-snow"),
    TV("Smart TV 55\"", "bi-tv"),
    MINIBAR("Minibar & tủ lạnh", "bi-cup-hot"),
    JACUZZI("Bồn Jacuzzi", "bi-droplet-half"),
    BREAKFAST("Bữa sáng miễn phí", "bi-egg-fried"),
    BUTLER("Butler 24/7", "bi-person-badge"),
    POOL("Hồ bơi riêng", "bi-water"),
    GYM("Phòng gym", "bi-bicycle"),
    SPA("Dịch vụ Spa", "bi-flower1");

    private final String displayName;
    private final String icon;

    Amenity(String displayName, String icon) {
        this.displayName = displayName;
        this.icon = icon;
    }

    public String getDisplayName() { return displayName; }
    public String getIcon() { return icon; }
}
