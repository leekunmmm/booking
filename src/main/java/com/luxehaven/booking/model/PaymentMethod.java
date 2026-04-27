package com.luxehaven.booking.model;

public enum PaymentMethod {
    MOMO("Ví MoMo"),
    BANK_TRANSFER("Chuyển khoản ngân hàng");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
