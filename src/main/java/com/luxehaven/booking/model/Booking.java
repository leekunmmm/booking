package com.luxehaven.booking.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {
    private Long id;
    private Long userId;
    private Long roomId;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private double totalPrice;
    private BookingStatus status;
    private PaymentStatus paymentStatus;
    private PaymentMethod paymentMethod;
    private String customerFullName;
    private String customerPhone;
    private String customerEmail;
    private String customerNote;
    private LocalDateTime createdAt;
    private String voucherCode;

    private transient Room room;
    private transient User user;

    public long getNumberOfNights() {
        if (checkIn == null || checkOut == null) return 0;
        return java.time.temporal.ChronoUnit.DAYS.between(checkIn, checkOut);
    }
}
