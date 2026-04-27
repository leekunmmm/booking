package com.luxehaven.booking.repository;

import com.luxehaven.booking.model.Voucher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class VoucherRepository {

    private final JdbcTemplate jdbc;

    public VoucherRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final RowMapper<Voucher> ROW_MAPPER = (rs, n) -> {
        String validUntilStr = rs.getString("valid_until");
        String targetTier = null;
        try { targetTier = rs.getString("target_tier"); } catch (Exception ignored) {}
        return Voucher.builder()
                .id(rs.getLong("id"))
                .code(rs.getString("code"))
                .discountPercent(rs.getInt("discount_percent"))
                .validUntil(validUntilStr != null ? LocalDate.parse(validUntilStr) : null)
                .maxUses(rs.getInt("max_uses"))
                .usedCount(rs.getInt("used_count"))
                .active(rs.getInt("active") == 1)
                .targetTier(targetTier)
                .build();
    };

    public List<Voucher> findAll() {
        return jdbc.query("SELECT * FROM vouchers ORDER BY id DESC", ROW_MAPPER);
    }

    public Optional<Voucher> findByCode(String code) {
        return jdbc.query("SELECT * FROM vouchers WHERE UPPER(code) = UPPER(?)", ROW_MAPPER, code)
                .stream().findFirst();
    }

    public Optional<Voucher> findById(Long id) {
        return jdbc.query("SELECT * FROM vouchers WHERE id = ?", ROW_MAPPER, id)
                .stream().findFirst();
    }

    public boolean existsByCode(String code) {
        Integer c = jdbc.queryForObject("SELECT COUNT(*) FROM vouchers WHERE UPPER(code) = UPPER(?)",
                Integer.class, code);
        return c != null && c > 0;
    }

    public Voucher save(Voucher v) {
        if (v.getId() == null) return insert(v);
        update(v);
        return v;
    }

    private Voucher insert(Voucher v) {
        KeyHolder kh = new GeneratedKeyHolder();
        String sql = "INSERT INTO vouchers(code, discount_percent, valid_until, max_uses, used_count, active, target_tier) VALUES(?,?,?,?,?,?,?)";
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, v.getCode().toUpperCase());
            ps.setInt(2, v.getDiscountPercent());
            ps.setString(3, v.getValidUntil() != null ? v.getValidUntil().toString() : null);
            ps.setInt(4, v.getMaxUses());
            ps.setInt(5, v.getUsedCount());
            ps.setInt(6, v.isActive() ? 1 : 0);
            ps.setString(7, v.getTargetTier());
            return ps;
        }, kh);
        Number key = kh.getKey();
        if (key != null) v.setId(key.longValue());
        return v;
    }

    private void update(Voucher v) {
        jdbc.update("UPDATE vouchers SET code=?, discount_percent=?, valid_until=?, max_uses=?, used_count=?, active=?, target_tier=? WHERE id=?",
                v.getCode().toUpperCase(), v.getDiscountPercent(),
                v.getValidUntil() != null ? v.getValidUntil().toString() : null,
                v.getMaxUses(), v.getUsedCount(), v.isActive() ? 1 : 0,
                v.getTargetTier(), v.getId());
    }

    public void incrementUsedCount(Long id) {
        jdbc.update("UPDATE vouchers SET used_count = used_count + 1 WHERE id = ?", id);
    }

    public boolean hasUserUsed(Long voucherId, Long userId) {
        Integer c = jdbc.queryForObject(
                "SELECT COUNT(*) FROM voucher_usage WHERE voucher_id = ? AND user_id = ?",
                Integer.class, voucherId, userId);
        return c != null && c > 0;
    }

    public java.util.Set<Long> findUsedVoucherIdsByUser(Long userId) {
        return new java.util.HashSet<>(jdbc.queryForList(
                "SELECT voucher_id FROM voucher_usage WHERE user_id = ?",
                Long.class, userId));
    }

    public void recordUsage(Long voucherId, Long userId) {
        jdbc.update("INSERT OR IGNORE INTO voucher_usage(voucher_id, user_id) VALUES(?,?)", voucherId, userId);
    }

    public void deleteById(Long id) {
        jdbc.update("DELETE FROM vouchers WHERE id = ?", id);
    }

    // ===== Saved vouchers =====
    public boolean hasSavedVoucher(Long voucherId, Long userId) {
        Integer c = jdbc.queryForObject(
                "SELECT COUNT(*) FROM saved_vouchers WHERE voucher_id = ? AND user_id = ?",
                Integer.class, voucherId, userId);
        return c != null && c > 0;
    }

    public void saveVoucher(Long voucherId, Long userId) {
        jdbc.update("INSERT OR IGNORE INTO saved_vouchers(voucher_id, user_id) VALUES(?,?)",
                voucherId, userId);
    }

    public void removeSavedVoucher(Long voucherId, Long userId) {
        jdbc.update("DELETE FROM saved_vouchers WHERE voucher_id = ? AND user_id = ?",
                voucherId, userId);
    }

    public List<Voucher> findSavedByUserId(Long userId) {
        return jdbc.query(
                "SELECT v.* FROM vouchers v " +
                "JOIN saved_vouchers sv ON v.id = sv.voucher_id " +
                "WHERE sv.user_id = ? ORDER BY sv.saved_at DESC",
                ROW_MAPPER, userId);
    }
}
