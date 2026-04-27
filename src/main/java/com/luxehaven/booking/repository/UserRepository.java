package com.luxehaven.booking.repository;

import com.luxehaven.booking.model.Role;
import com.luxehaven.booking.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbc;

    public UserRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final RowMapper<User> ROW_MAPPER = (rs, n) -> {
        User u = User.builder()
            .id(rs.getLong("id"))
            .username(rs.getString("username"))
            .password(rs.getString("password"))
            .fullName(rs.getString("full_name"))
            .email(rs.getString("email"))
            .phone(rs.getString("phone"))
            .role(Role.valueOf(rs.getString("role")))
            .createdAt(parseTs(rs.getString("created_at")))
            .build();
        try { u.setAvatarUrl(rs.getString("avatar_url")); } catch (Exception ignored) {}
        try { u.setMemberTier(rs.getString("member_tier")); } catch (Exception ignored) {}
        return u;
    };

    private static LocalDateTime parseTs(String s) {
        if (s == null) return null;
        try {
            return LocalDateTime.parse(s.replace(' ', 'T'));
        } catch (Exception e) {
            return null;
        }
    }

    public Optional<User> findById(Long id) {
        return jdbc.query("SELECT * FROM users WHERE id = ?", ROW_MAPPER, id)
                .stream().findFirst();
    }

    public Optional<User> findByUsername(String username) {
        return jdbc.query("SELECT * FROM users WHERE username = ?", ROW_MAPPER, username)
                .stream().findFirst();
    }

    public Optional<User> findByEmail(String email) {
        return jdbc.query("SELECT * FROM users WHERE email = ?", ROW_MAPPER, email)
                .stream().findFirst();
    }

    public List<User> findAll() {
        return jdbc.query("SELECT * FROM users ORDER BY id", ROW_MAPPER);
    }

    public boolean existsByUsername(String username) {
        Integer c = jdbc.queryForObject(
                "SELECT COUNT(*) FROM users WHERE username = ?", Integer.class, username);
        return c != null && c > 0;
    }

    public boolean existsByEmail(String email) {
        Integer c = jdbc.queryForObject(
                "SELECT COUNT(*) FROM users WHERE email = ?", Integer.class, email);
        return c != null && c > 0;
    }

    public User save(User user) {
        if (user.getId() == null) {
            return insert(user);
        }
        update(user);
        return user;
    }

    private User insert(User u) {
        KeyHolder kh = new GeneratedKeyHolder();
        String sql = "INSERT INTO users(username, password, full_name, email, phone, role, created_at) " +
                     "VALUES(?, ?, ?, ?, ?, ?, ?)";
        LocalDateTime now = u.getCreatedAt() != null ? u.getCreatedAt() : LocalDateTime.now();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, u.getUsername());
            ps.setString(2, u.getPassword());
            ps.setString(3, u.getFullName());
            ps.setString(4, u.getEmail());
            ps.setString(5, u.getPhone());
            ps.setString(6, u.getRole().name());
            ps.setTimestamp(7, Timestamp.valueOf(now));
            return ps;
        }, kh);
        Number key = kh.getKey();
        if (key != null) u.setId(key.longValue());
        u.setCreatedAt(now);
        return u;
    }

    private void update(User u) {
        jdbc.update("UPDATE users SET username=?, password=?, full_name=?, email=?, phone=?, role=?, avatar_url=? WHERE id=?",
                u.getUsername(), u.getPassword(), u.getFullName(), u.getEmail(), u.getPhone(),
                u.getRole().name(), u.getAvatarUrl(), u.getId());
    }

    public void updateProfile(Long id, String fullName, String email, String phone, String avatarUrl) {
        jdbc.update("UPDATE users SET full_name=?, email=?, phone=?, avatar_url=? WHERE id=?",
                fullName, email, phone, avatarUrl, id);
    }

    public void updateMemberTier(Long id, String tier) {
        jdbc.update("UPDATE users SET member_tier = ? WHERE id = ?", tier, id);
    }

    public void deleteById(Long id) {
        jdbc.update("DELETE FROM users WHERE id = ?", id);
    }
}
