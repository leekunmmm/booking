package com.luxehaven.booking.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Voucher {
    private Long id;
    private String code;
    private int discountPercent;
    private LocalDate validUntil;
    private int maxUses;
    private int usedCount;
    private boolean active;
    private String targetTier; // null = all members, BRONZE/SILVER/GOLD/DIAMOND/VIP = tier-specific

    public boolean isUsable() {
        return active
                && (validUntil == null || !LocalDate.now().isAfter(validUntil))
                && usedCount < maxUses;
    }
}
