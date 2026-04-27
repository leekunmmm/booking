package com.luxehaven.booking.repository;

import com.luxehaven.booking.model.Room;
import com.luxehaven.booking.model.RoomStatus;
import com.luxehaven.booking.model.RoomType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class RoomRepository {

    private final JdbcTemplate jdbc;

    public RoomRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final RowMapper<Room> ROW_MAPPER = (rs, n) -> Room.builder()
            .id(rs.getLong("id"))
            .roomNumber(rs.getString("room_number"))
            .type(RoomType.valueOf(rs.getString("type")))
            .pricePerNight(rs.getDouble("price_per_night"))
            .capacity(rs.getInt("capacity"))
            .description(rs.getString("description"))
            .status(RoomStatus.valueOf(rs.getString("status")))
            .imagesRaw(rs.getString("images"))
            .amenitiesRaw(rs.getString("amenities"))
            .build();

    public List<Room> findAll() {
        return jdbc.query("SELECT * FROM rooms ORDER BY room_number", ROW_MAPPER);
    }

    public List<Room> findAllAvailable() {
        return jdbc.query("SELECT * FROM rooms WHERE status = 'AVAILABLE' ORDER BY room_number", ROW_MAPPER);
    }

    public List<Room> findFiltered(RoomType type, RoomStatus status, String search) {
        StringBuilder sql = new StringBuilder("SELECT * FROM rooms WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (type != null) { sql.append(" AND type = ?"); params.add(type.name()); }
        if (status != null) { sql.append(" AND status = ?"); params.add(status.name()); }
        if (search != null && !search.isBlank()) {
            sql.append(" AND LOWER(room_number) LIKE ?");
            params.add("%" + search.trim().toLowerCase() + "%");
        }
        sql.append(" ORDER BY room_number");
        return jdbc.query(sql.toString(), ROW_MAPPER, params.toArray());
    }

    public Optional<Room> findById(Long id) {
        return jdbc.query("SELECT * FROM rooms WHERE id = ?", ROW_MAPPER, id)
                .stream().findFirst();
    }

    public boolean existsByRoomNumber(String roomNumber) {
        Integer c = jdbc.queryForObject(
                "SELECT COUNT(*) FROM rooms WHERE room_number = ?", Integer.class, roomNumber);
        return c != null && c > 0;
    }

    public List<Room> findAvailableInRange(LocalDate checkIn, LocalDate checkOut, RoomType type) {
        StringBuilder sql = new StringBuilder(
                "SELECT r.* FROM rooms r " +
                "WHERE r.status IN ('AVAILABLE','OCCUPIED') " +
                "AND r.id NOT IN (" +
                "  SELECT b.room_id FROM bookings b " +
                "  WHERE b.status IN ('PENDING','CONFIRMED') " +
                "  AND NOT (b.check_out <= ? OR b.check_in >= ?)" +
                ")");
        if (type != null) sql.append(" AND r.type = ?");
        sql.append(" ORDER BY r.room_number");

        if (type != null) {
            return jdbc.query(sql.toString(), ROW_MAPPER,
                    checkIn.toString(), checkOut.toString(), type.name());
        }
        return jdbc.query(sql.toString(), ROW_MAPPER,
                checkIn.toString(), checkOut.toString());
    }

    public Room save(Room room) {
        if (room.getId() == null) return insert(room);
        update(room);
        return room;
    }

    private Room insert(Room r) {
        KeyHolder kh = new GeneratedKeyHolder();
        String sql = "INSERT INTO rooms(room_number, type, price_per_night, capacity, description, status, images, amenities) " +
                     "VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, r.getRoomNumber());
            ps.setString(2, r.getType().name());
            ps.setDouble(3, r.getPricePerNight());
            ps.setInt(4, r.getCapacity());
            ps.setString(5, r.getDescription());
            ps.setString(6, r.getStatus().name());
            ps.setString(7, r.getImagesRaw());
            ps.setString(8, r.getAmenitiesRaw());
            return ps;
        }, kh);
        Number key = kh.getKey();
        if (key != null) r.setId(key.longValue());
        return r;
    }

    private void update(Room r) {
        jdbc.update("UPDATE rooms SET room_number=?, type=?, price_per_night=?, capacity=?, description=?, status=?, images=?, amenities=? WHERE id=?",
                r.getRoomNumber(), r.getType().name(), r.getPricePerNight(),
                r.getCapacity(), r.getDescription(), r.getStatus().name(),
                r.getImagesRaw(), r.getAmenitiesRaw(), r.getId());
    }

    public void updateStatus(Long roomId, RoomStatus status) {
        jdbc.update("UPDATE rooms SET status = ? WHERE id = ?", status.name(), roomId);
    }

    public void deleteById(Long id) {
        jdbc.update("DELETE FROM rooms WHERE id = ?", id);
    }

    public boolean hasActiveBookings(Long roomId) {
        Integer c = jdbc.queryForObject(
                "SELECT COUNT(*) FROM bookings WHERE room_id = ? AND status IN ('PENDING','CONFIRMED')",
                Integer.class, roomId);
        return c != null && c > 0;
    }
}
