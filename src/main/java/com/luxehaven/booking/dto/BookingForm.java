package com.luxehaven.booking.dto;

import com.luxehaven.booking.model.PaymentMethod;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class BookingForm {
    private Long roomId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate checkIn;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate checkOut;

    private String voucherCode;
    private PaymentMethod paymentMethod;
    private String customerFullName;
    private String customerPhone;
    private String customerEmail;
    private String customerNote;
}
