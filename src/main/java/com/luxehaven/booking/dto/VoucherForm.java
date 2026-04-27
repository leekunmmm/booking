package com.luxehaven.booking.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class VoucherForm {
    private Long id;
    private String code;
    private int discountPercent;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate validUntil;
    private int maxUses = 100;
    private boolean active = true;
    private String targetTier; // null = tất cả
}
