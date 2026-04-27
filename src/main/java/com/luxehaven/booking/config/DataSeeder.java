package com.luxehaven.booking.config;

import com.luxehaven.booking.model.Role;
import com.luxehaven.booking.model.RoomStatus;
import com.luxehaven.booking.model.RoomType;
import com.luxehaven.booking.model.User;
import com.luxehaven.booking.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbc;

    public DataSeeder(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, JdbcTemplate jdbc) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jdbc = jdbc;
    }

    @Override
    public void run(String... args) {
        migrateSchema();
        seedUsers();
        seedSampleImages();
        seedSampleAmenities();
        seedVouchers();
        seedMoreRooms();
    }

    private void migrateSchema() {
        tryAlter("ALTER TABLE rooms ADD COLUMN images TEXT");
        tryAlter("ALTER TABLE rooms ADD COLUMN amenities TEXT");
        tryAlter("ALTER TABLE bookings ADD COLUMN voucher_code TEXT");
        tryAlter("ALTER TABLE bookings ADD COLUMN payment_method TEXT NOT NULL DEFAULT 'BANK_TRANSFER'");
        tryAlter("UPDATE bookings SET payment_method = 'BANK_TRANSFER' WHERE payment_method IS NULL OR payment_method = ''");
        tryAlter("ALTER TABLE bookings ADD COLUMN customer_full_name TEXT");
        tryAlter("ALTER TABLE bookings ADD COLUMN customer_phone TEXT");
        tryAlter("ALTER TABLE bookings ADD COLUMN customer_email TEXT");
        tryAlter("ALTER TABLE bookings ADD COLUMN customer_note TEXT");
        tryAlter("ALTER TABLE users ADD COLUMN avatar_url TEXT");
        tryAlter("ALTER TABLE users ADD COLUMN member_tier TEXT");
        tryAlter("CREATE TABLE IF NOT EXISTS vouchers (" +
                 "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                 "code TEXT NOT NULL UNIQUE, " +
                 "discount_percent INTEGER NOT NULL, " +
                 "valid_until TEXT, " +
                 "max_uses INTEGER NOT NULL DEFAULT 100, " +
                 "used_count INTEGER NOT NULL DEFAULT 0, " +
                 "active INTEGER NOT NULL DEFAULT 1)");
        tryAlter("ALTER TABLE vouchers ADD COLUMN target_tier TEXT");
        tryAlter("CREATE TABLE IF NOT EXISTS voucher_usage (" +
                 "voucher_id INTEGER NOT NULL, " +
                 "user_id    INTEGER NOT NULL, " +
                 "PRIMARY KEY (voucher_id, user_id), " +
                 "FOREIGN KEY (voucher_id) REFERENCES vouchers(id), " +
                 "FOREIGN KEY (user_id) REFERENCES users(id))");
        tryAlter("CREATE TABLE IF NOT EXISTS saved_vouchers (" +
                 "voucher_id INTEGER NOT NULL, " +
                 "user_id    INTEGER NOT NULL, " +
                 "saved_at   TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                 "PRIMARY KEY (voucher_id, user_id), " +
                 "FOREIGN KEY (voucher_id) REFERENCES vouchers(id) ON DELETE CASCADE, " +
                 "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE)");
    }

    private void tryAlter(String sql) {
        try { jdbc.execute(sql); } catch (Exception ignored) {}
    }

    private void seedUsers() {
        seedIfMissing("admin", "admin123", "Quản trị viên", "admin@luxehaven.vn", "0900000000", Role.ADMIN);
        seedIfMissing("customer1", "pass123", "Nguyễn Văn A", "a@example.com", "0911111111", Role.CUSTOMER);
    }

    private void seedIfMissing(String username, String rawPassword, String fullName,
                               String email, String phone, Role role) {
        if (userRepository.existsByUsername(username)) return;
        User u = User.builder()
                .username(username)
                .password(passwordEncoder.encode(rawPassword))
                .fullName(fullName)
                .email(email)
                .phone(phone)
                .role(role)
                .build();
        userRepository.save(u);
        log.info("Seeded user: {} ({})", username, role);
    }

    private void seedSampleImages() {
        setImages("101",
            "https://images.unsplash.com/photo-1631049307264-da0ec9d70304?w=1200&q=80," +
            "https://images.unsplash.com/photo-1631049552057-403cdb8f0658?w=1200&q=80," +
            "https://images.unsplash.com/photo-1560185007-cde436f6a4d0?w=1200&q=80");
        setImages("102",
            "https://images.unsplash.com/photo-1611892440504-42a792e24d32?w=1200&q=80," +
            "https://images.unsplash.com/photo-1631049307264-da0ec9d70304?w=1200&q=80," +
            "https://images.unsplash.com/photo-1560185007-cde436f6a4d0?w=1200&q=80");
        setImages("201",
            "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=1200&q=80," +
            "https://images.unsplash.com/photo-1590490360182-c33d57733427?w=1200&q=80," +
            "https://images.unsplash.com/photo-1601565415267-724db0cfc0c5?w=1200&q=80," +
            "https://images.unsplash.com/photo-1564501049412-61c2a3083791?w=1200&q=80");
        setImages("202",
            "https://images.unsplash.com/photo-1590490360182-c33d57733427?w=1200&q=80," +
            "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=1200&q=80," +
            "https://images.unsplash.com/photo-1564501049412-61c2a3083791?w=1200&q=80");
        setImages("301",
            "https://images.unsplash.com/photo-1571003123894-1f0594d2b5d9?w=1200&q=80," +
            "https://images.unsplash.com/photo-1549294413-26f195200c16?w=1200&q=80," +
            "https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=1200&q=80," +
            "https://images.unsplash.com/photo-1601565415267-724db0cfc0c5?w=1200&q=80," +
            "https://images.unsplash.com/photo-1596394516093-501ba68a0ba6?w=1200&q=80");
        setImages("302",
            "https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=1200&q=80," +
            "https://images.unsplash.com/photo-1571003123894-1f0594d2b5d9?w=1200&q=80," +
            "https://images.unsplash.com/photo-1549294413-26f195200c16?w=1200&q=80," +
            "https://images.unsplash.com/photo-1596394516093-501ba68a0ba6?w=1200&q=80");
    }

    private void setImages(String roomNumber, String images) {
        try {
            jdbc.update("UPDATE rooms SET images = ? WHERE room_number = ? AND (images IS NULL OR images = '')",
                        images, roomNumber);
        } catch (Exception ignored) {}
    }

    private void seedSampleAmenities() {
        setAmenities("101", "WIFI,AC,TV,MINIBAR");
        setAmenities("102", "WIFI,AC,TV,MINIBAR");
        setAmenities("201", "WIFI,AC,TV,MINIBAR,JACUZZI,BREAKFAST");
        setAmenities("202", "WIFI,AC,TV,MINIBAR,JACUZZI,BREAKFAST");
        setAmenities("301", "WIFI,AC,TV,MINIBAR,JACUZZI,BREAKFAST,BUTLER,POOL,SPA");
        setAmenities("302", "WIFI,AC,TV,MINIBAR,JACUZZI,BREAKFAST,BUTLER,POOL,GYM,SPA");
    }

    private void setAmenities(String roomNumber, String amenities) {
        try {
            jdbc.update("UPDATE rooms SET amenities = ? WHERE room_number = ? AND (amenities IS NULL OR amenities = '')",
                        amenities, roomNumber);
        } catch (Exception ignored) {}
    }

    private void seedVouchers() {
        try {
            Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM vouchers", Integer.class);
            if (count != null && count == 0) {
                jdbc.update("INSERT INTO vouchers(code, discount_percent, valid_until, max_uses, used_count, active) VALUES(?,?,?,?,?,?)",
                        "WELCOME10", 10, "2026-12-31", 100, 0, 1);
                jdbc.update("INSERT INTO vouchers(code, discount_percent, valid_until, max_uses, used_count, active) VALUES(?,?,?,?,?,?)",
                        "SUMMER20", 20, "2026-08-31", 50, 0, 1);
                jdbc.update("INSERT INTO vouchers(code, discount_percent, valid_until, max_uses, used_count, active) VALUES(?,?,?,?,?,?)",
                        "VIP30", 30, "2026-12-31", 20, 0, 1);
                log.info("Seeded sample vouchers");
            }
        } catch (Exception e) {
            log.warn("Could not seed vouchers: {}", e.getMessage());
        }
    }

    private void seedMoreRooms() {
        try {
            int seeded = 0;
            for (int floor = 4; floor <= 13 && seeded < 50; floor++) {
                for (int no = 1; no <= 10 && seeded < 50; no++) {
                    String roomNumber = floor + String.format("%02d", no);
                    Integer exists = jdbc.queryForObject(
                            "SELECT COUNT(*) FROM rooms WHERE room_number = ?",
                            Integer.class, roomNumber
                    );
                    if (exists != null && exists > 0) continue;

                    RoomType type = (no <= 5) ? RoomType.STANDARD : (no <= 8 ? RoomType.DELUXE : RoomType.SUITE);
                    double basePrice = switch (type) {
                        case STANDARD -> 550_000;
                        case DELUXE -> 980_000;
                        case SUITE -> 1_950_000;
                    };
                    int capacity = switch (type) {
                        case STANDARD -> 2;
                        case DELUXE -> 3;
                        case SUITE -> 4;
                    };
                    double price = basePrice + floor * 20_000 + no * 10_000;
                    String desc = "Phòng " + type.name() + " tầng " + floor +
                            ", thiết kế hiện đại, đầy đủ tiện nghi và không gian yên tĩnh.";
                    String images = "https://images.unsplash.com/photo-1611892440504-42a792e24d32?w=1200&q=80," +
                            "https://images.unsplash.com/photo-1631049307264-da0ec9d70304?w=1200&q=80," +
                            "https://images.unsplash.com/photo-1560185007-cde436f6a4d0?w=1200&q=80";
                    String amenities = switch (type) {
                        case STANDARD -> "WIFI,AC,TV,MINIBAR";
                        case DELUXE -> "WIFI,AC,TV,MINIBAR,JACUZZI,BREAKFAST";
                        case SUITE -> "WIFI,AC,TV,MINIBAR,JACUZZI,BREAKFAST,BUTLER,POOL";
                    };

                    jdbc.update("INSERT INTO rooms(room_number, type, price_per_night, capacity, description, status, images, amenities) " +
                                    "VALUES(?,?,?,?,?,?,?,?)",
                            roomNumber, type.name(), price, capacity, desc, RoomStatus.AVAILABLE.name(), images, amenities);
                    seeded++;
                }
            }
            if (seeded > 0) {
                log.info("Seeded {} more rooms", seeded);
            }
        } catch (Exception e) {
            log.warn("Could not seed more rooms: {}", e.getMessage());
        }
    }
}
