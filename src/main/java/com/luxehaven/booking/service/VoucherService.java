package com.luxehaven.booking.service;

import com.luxehaven.booking.dto.VoucherForm;
import com.luxehaven.booking.exception.BusinessException;
import com.luxehaven.booking.model.MemberTier;
import com.luxehaven.booking.model.Voucher;
import com.luxehaven.booking.repository.VoucherRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VoucherService {

    private final VoucherRepository voucherRepository;
    private final UserService userService;

    public VoucherService(VoucherRepository voucherRepository, UserService userService) {
        this.voucherRepository = voucherRepository;
        this.userService = userService;
    }

    public List<Voucher> findAll() {
        return voucherRepository.findAll();
    }

    public Voucher findById(Long id) {
        return voucherRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy voucher"));
    }

    public Optional<Voucher> findByCode(String code) {
        if (code == null || code.isBlank()) return Optional.empty();
        return voucherRepository.findByCode(code.trim());
    }

    public Voucher validateAndGet(String code, Long userId) {
        if (code == null || code.isBlank()) return null;
        Voucher v = voucherRepository.findByCode(code.trim())
                .orElseThrow(() -> new BusinessException("Mã voucher không hợp lệ"));
        if (!v.isUsable()) throw new BusinessException("Voucher đã hết hạn hoặc đã dùng hết");

        if (v.getTargetTier() != null) {
            MemberTier userTier = userId != null ? userService.calculateTier(userId) : null;
            String userTierName = userTier != null ? userTier.name() : null;
            if (!v.getTargetTier().equals(userTierName)) {
                throw new BusinessException("Voucher này chỉ áp dụng cho thành viên hạng "
                        + tierDisplayName(v.getTargetTier()));
            }
        }

        if (userId != null && voucherRepository.hasUserUsed(v.getId(), userId)) {
            throw new BusinessException("Bạn đã sử dụng voucher này rồi");
        }
        return v;
    }

    public void use(Voucher voucher, Long userId) {
        if (voucher == null) return;
        voucherRepository.incrementUsedCount(voucher.getId());
        if (userId != null) voucherRepository.recordUsage(voucher.getId(), userId);
    }

    public Voucher create(VoucherForm form) {
        validateForm(form);
        if (voucherRepository.existsByCode(form.getCode())) {
            throw new BusinessException("Mã voucher đã tồn tại");
        }
        Voucher v = Voucher.builder()
                .code(form.getCode().trim().toUpperCase())
                .discountPercent(form.getDiscountPercent())
                .validUntil(form.getValidUntil())
                .maxUses(form.getMaxUses() > 0 ? form.getMaxUses() : 100)
                .usedCount(0)
                .active(form.isActive())
                .targetTier(form.getTargetTier() != null && !form.getTargetTier().isBlank()
                        ? form.getTargetTier() : null)
                .build();
        return voucherRepository.save(v);
    }

    public Voucher update(Long id, VoucherForm form) {
        validateForm(form);
        Voucher existing = findById(id);
        if (!existing.getCode().equalsIgnoreCase(form.getCode())
                && voucherRepository.existsByCode(form.getCode())) {
            throw new BusinessException("Mã voucher đã tồn tại");
        }
        existing.setCode(form.getCode().trim().toUpperCase());
        existing.setDiscountPercent(form.getDiscountPercent());
        existing.setValidUntil(form.getValidUntil());
        existing.setMaxUses(form.getMaxUses() > 0 ? form.getMaxUses() : 100);
        existing.setActive(form.isActive());
        existing.setTargetTier(form.getTargetTier() != null && !form.getTargetTier().isBlank()
                ? form.getTargetTier() : null);
        return voucherRepository.save(existing);
    }

    public void delete(Long id) {
        voucherRepository.deleteById(id);
    }

    // ===== Kho voucher của người dùng =====
    public void saveForUser(Long voucherId, Long userId) {
        Voucher v = voucherRepository.findById(voucherId)
                .orElseThrow(() -> new BusinessException("Voucher không tồn tại"));
        if (!v.isUsable()) throw new BusinessException("Voucher đã hết hạn hoặc hết lượt dùng");
        if (voucherRepository.hasSavedVoucher(voucherId, userId))
            throw new BusinessException("Bạn đã lưu voucher này rồi");
        voucherRepository.saveVoucher(voucherId, userId);
    }

    public List<Voucher> getSavedByUser(Long userId) {
        return voucherRepository.findSavedByUserId(userId);
    }

    public void removeSavedForUser(Long voucherId, Long userId) {
        voucherRepository.removeSavedVoucher(voucherId, userId);
    }

    public java.util.Set<Long> getUsedVoucherIdsByUser(Long userId) {
        return voucherRepository.findUsedVoucherIdsByUser(userId);
    }

    private void validateForm(VoucherForm f) {
        if (f.getCode() == null || f.getCode().isBlank()) throw new BusinessException("Mã voucher không được trống");
        if (f.getDiscountPercent() < 1 || f.getDiscountPercent() > 100) throw new BusinessException("Giảm giá phải từ 1% đến 100%");
    }

    private String tierDisplayName(String tier) {
        return switch (tier) {
            case "BRONZE"  -> "Bronze";
            case "SILVER"  -> "Silver";
            case "GOLD"    -> "Gold";
            case "DIAMOND" -> "Diamond";
            case "VIP"     -> "VIP";
            default -> tier;
        };
    }
}
