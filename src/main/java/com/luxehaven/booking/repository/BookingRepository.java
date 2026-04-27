package com.luxehaven.booking.repository;

import com.luxehaven.booking.model.Booking;
import com.luxehaven.booking.model.BookingStatus;
import com.luxehaven.booking.model.PaymentMethod;
import com.luxehaven.booking.model.PaymentStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class BookingRepository {

    private final JdbcTemplate jdbc;

    public BookingRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final RowMapper<Booking> ROW_MAPPER = (rs, n) -> Booking.builder()
            .id(rs.getLong("id"))
            .userId(rs.getLong("user_id"))
            .roomId(rs.getLong("room_id"))
            .checkIn(LocalDate.parse(rs.getString("check_in")))
            .checkOut(LocalDate.parse(rs.getString("check_out")))
            .totalPrice(rs.getDouble("total_price"))
            .status(BookingStatus.valueOf(rs.getString("status")))
            .paymentStatus(PaymentStatus.valueOf(rs.getString("payment_status")))
            .paymentMethod(parsePaymentMethod(rs.getString("payment_method")))
            .customerFullName(rs.getString("customer_full_name"))
            .customerPhone(rs.getString("customer_phone"))
            .customerEmail(rs.getString("customer_email"))
            .customerNote(rs.getString("customer_note"))
            .createdAt(parseTs(rs.getString("created_at")))
            .voucherCode(rs.getString("voucher_code"))
            .build();

    private static LocalDateTime parseTs(String s) {
        if (s == null) return null;
        try {
            return LocalDateTime.parse(s.replace(' ', 'T'));
        } catch (Exception e) {
            return null;
        }
    }

    private static PaymentMethod parsePaymentMethod(String value) {
        if (value == null || value.isBlank()) {
            return PaymentMethod.BANK_TRANSFER;
        }
        try {
            return PaymentMethod.valueOf(value);
        } catch (Exception e) {
            return PaymentMethod.BANK_TRANSFER;
        }
    }

    public List<Booking> findAll() {
        return jdbc.query("SELECT * FROM bookings ORDER BY id DESC", ROW_MAPPER);
    }

    public List<Booking> findByUserId(Long userId) {
        return jdbc.query("SELECT * FROM bookings WHERE user_id = ? ORDER BY id DESC",
                ROW_MAPPER, userId);
    }

    public List<Booking> findByStatus(BookingStatus status) {
        return jdbc.query("SELECT * FROM bookings WHERE status = ? ORDER BY id DESC",
                ROW_MAPPER, status.name());
    }

    public Optional<Booking> findById(Long id) {
        return jdbc.query("SELECT * FROM bookings WHERE id = ?", ROW_MAPPER, id)
                .stream().findFirst();
    }

    /**
     * Trả về true nếu phòng đã có booking PENDING/CONFIRMED trùng khoảng [checkIn, checkOut).
     * (Loại trừ booking có id=excludeBookingId nếu có, để dùng khi cập nhật.)
     */
    public boolean hasOverlap(Long roomId, LocalDate checkIn, LocalDate checkOut, Long excludeBookingId) {
        String sql = "SELECT COUNT(*) FROM bookings " +
                     "WHERE room_id = ? AND status IN ('PENDING','CONFIRMED') " +
                     "AND NOT (check_out <= ? OR check_in >= ?)" +
                     (excludeBookingId != null ? " AND id <> ?" : "");
        Integer c;
        if (excludeBookingId != null) {
            c = jdbc.queryForObject(sql, Integer.class,
                    roomId, checkIn.toString(), checkOut.toString(), excludeBookingId);
        } else {
            c = jdbc.queryForObject(sql, Integer.class,
                    roomId, checkIn.toString(), checkOut.toString());
        }
        return c != null && c > 0;
    }

    public Booking save(Booking b) {
        if (b.getId() == null) return insert(b);
        update(b);
        return b;
    }

    private Booking insert(Booking b) {
        KeyHolder kh = new GeneratedKeyHolder();
        String sql = "INSERT INTO bookings(user_id, room_id, check_in, check_out, total_price, " +
                     "status, payment_status, payment_method, customer_full_name, customer_phone, customer_email, customer_note, created_at, voucher_code) " +
                     "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        LocalDateTime now = b.getCreatedAt() != null ? b.getCreatedAt() : LocalDateTime.now();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, b.getUserId());
            ps.setLong(2, b.getRoomId());
            ps.setString(3, b.getCheckIn().toString());
            ps.setString(4, b.getCheckOut().toString());
            ps.setDouble(5, b.getTotalPrice());
            ps.setString(6, b.getStatus().name());
            ps.setString(7, b.getPaymentStatus().name());
            ps.setString(8, b.getPaymentMethod().name());
            ps.setString(9, b.getCustomerFullName());
            ps.setString(10, b.getCustomerPhone());
            ps.setString(11, b.getCustomerEmail());
            ps.setString(12, b.getCustomerNote());
            ps.setString(13, now.toString().replace('T', ' '));
            ps.setString(14, b.getVoucherCode());
            return ps;
        }, kh);
        Number key = kh.getKey();
        if (key != null) b.setId(key.longValue());
        b.setCreatedAt(now);
        return b;
    }

    private void update(Booking b) {
        jdbc.update("UPDATE bookings SET user_id=?, room_id=?, check_in=?, check_out=?, total_price=?, " +
                    "status=?, payment_status=?, payment_method=?, customer_full_name=?, customer_phone=?, customer_email=?, customer_note=? WHERE id=?",
                b.getUserId(), b.getRoomId(),
                b.getCheckIn().toString(), b.getCheckOut().toString(),
                b.getTotalPrice(), b.getStatus().name(), b.getPaymentStatus().name(), b.getPaymentMethod().name(),
                b.getCustomerFullName(), b.getCustomerPhone(), b.getCustomerEmail(), b.getCustomerNote(),
                b.getId());
    }

    public java.util.Set<Long> findBookedRoomIdsOnDate(LocalDate date) {
        return new java.util.HashSet<>(jdbc.queryForList(
                "SELECT DISTINCT room_id FROM bookings " +
                "WHERE status IN ('PENDING','CONFIRMED') " +
                "AND check_in <= ? AND check_out > ?",
                Long.class, date.toString(), date.toString()));
    }

    public boolean hasActiveBookingsExcluding(Long roomId, Long excludeBookingId) {
        Integer c = jdbc.queryForObject(
                "SELECT COUNT(*) FROM bookings WHERE room_id = ? AND status IN ('PENDING','CONFIRMED') AND id <> ?",
                Integer.class, roomId, excludeBookingId);
        return c != null && c > 0;
    }

    public int countByStatus(BookingStatus status) {
        Integer c = jdbc.queryForObject(
                "SELECT COUNT(*) FROM bookings WHERE status = ?", Integer.class, status.name());
        return c == null ? 0 : c;
    }

    public double totalRevenue() {
        Double r = jdbc.queryForObject(
                "SELECT COALESCE(SUM(total_price), 0) FROM bookings WHERE payment_status = 'PAID'",
                Double.class);
        return r == null ? 0 : r;
    }

    public double sumPaidByUserId(Long userId) {
        Double r = jdbc.queryForObject(
                "SELECT COALESCE(SUM(total_price), 0) FROM bookings WHERE user_id = ? AND payment_status = 'PAID'",
                Double.class, userId);
        return r == null ? 0 : r;
    }

    public int countByUserId(Long userId) {
        Integer c = jdbc.queryForObject(
                "SELECT COUNT(*) FROM bookings WHERE user_id = ?", Integer.class, userId);
        return c == null ? 0 : c;
    }
}
