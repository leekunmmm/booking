package com.luxehaven.booking.model;

public enum MemberTier {
    BRONZE("Bronze"),
    SILVER("Silver"),
    GOLD("Gold"),
    DIAMOND("Diamond"),
    VIP("VIP");

    private final String displayName;

    MemberTier(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
