-- ===== Du lieu mau cho LuxeHaven Booking =====
-- Tai khoan admin/customer duoc seed boi DataSeeder.java (de password BCrypt dung).

-- Seed phong (chi insert neu chua co)
INSERT INTO rooms (room_number, type, price_per_night, capacity, description, status)
SELECT '101', 'STANDARD', 500000, 2, 'Phong Standard tieu chuan, view san vuon, may lanh, TV, tu lanh.', 'AVAILABLE'
WHERE NOT EXISTS (SELECT 1 FROM rooms WHERE room_number = '101');

INSERT INTO rooms (room_number, type, price_per_night, capacity, description, status)
SELECT '102', 'STANDARD', 500000, 2, 'Phong Standard view ho boi, may lanh, TV.', 'AVAILABLE'
WHERE NOT EXISTS (SELECT 1 FROM rooms WHERE room_number = '102');

INSERT INTO rooms (room_number, type, price_per_night, capacity, description, status)
SELECT '201', 'DELUXE', 900000, 3, 'Phong Deluxe rong rai, ban cong rieng, mini-bar, bon tam.', 'AVAILABLE'
WHERE NOT EXISTS (SELECT 1 FROM rooms WHERE room_number = '201');

INSERT INTO rooms (room_number, type, price_per_night, capacity, description, status)
SELECT '202', 'DELUXE', 950000, 3, 'Phong Deluxe view bien, mini-bar, bon tam, may say toc.', 'AVAILABLE'
WHERE NOT EXISTS (SELECT 1 FROM rooms WHERE room_number = '202');

INSERT INTO rooms (room_number, type, price_per_night, capacity, description, status)
SELECT '301', 'SUITE', 1800000, 4, 'Phong Suite hang sang, phong khach rieng, bon Jacuzzi, bua sang mien phi.', 'AVAILABLE'
WHERE NOT EXISTS (SELECT 1 FROM rooms WHERE room_number = '301');

INSERT INTO rooms (room_number, type, price_per_night, capacity, description, status)
SELECT '302', 'SUITE', 2200000, 4, 'Phong Suite Tong Thong, view toan canh, dich vu butler 24/7.', 'AVAILABLE'
WHERE NOT EXISTS (SELECT 1 FROM rooms WHERE room_number = '302');
