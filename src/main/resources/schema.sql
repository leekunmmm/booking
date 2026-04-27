-- ===== Schema cho LuxeHaven Booking =====

CREATE TABLE IF NOT EXISTS users (
  id          INTEGER PRIMARY KEY AUTOINCREMENT,
  username    TEXT    NOT NULL UNIQUE,
  password    TEXT    NOT NULL,
  full_name   TEXT    NOT NULL,
  email       TEXT    NOT NULL UNIQUE,
  phone       TEXT,
  role        TEXT    NOT NULL DEFAULT 'CUSTOMER',
  created_at  TEXT    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS rooms (
  id              INTEGER PRIMARY KEY AUTOINCREMENT,
  room_number     TEXT    NOT NULL UNIQUE,
  type            TEXT    NOT NULL,
  price_per_night REAL    NOT NULL CHECK (price_per_night > 0),
  capacity        INTEGER NOT NULL CHECK (capacity > 0),
  description     TEXT,
  status          TEXT    NOT NULL DEFAULT 'AVAILABLE',
  images          TEXT,
  amenities       TEXT
);

CREATE TABLE IF NOT EXISTS bookings (
  id              INTEGER PRIMARY KEY AUTOINCREMENT,
  user_id         INTEGER NOT NULL,
  room_id         INTEGER NOT NULL,
  check_in        DATE    NOT NULL,
  check_out       DATE    NOT NULL,
  total_price     REAL    NOT NULL,
  status          TEXT    NOT NULL DEFAULT 'PENDING',
  payment_status  TEXT    NOT NULL DEFAULT 'UNPAID',
  payment_method  TEXT    NOT NULL DEFAULT 'BANK_TRANSFER',
  customer_full_name TEXT,
  customer_phone  TEXT,
  customer_email  TEXT,
  customer_note   TEXT,
  created_at      TEXT    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  voucher_code    TEXT,
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (room_id) REFERENCES rooms(id),
  CHECK (check_out > check_in)
);

CREATE TABLE IF NOT EXISTS vouchers (
  id               INTEGER PRIMARY KEY AUTOINCREMENT,
  code             TEXT    NOT NULL UNIQUE,
  discount_percent INTEGER NOT NULL CHECK (discount_percent BETWEEN 1 AND 100),
  valid_until      TEXT,
  max_uses         INTEGER NOT NULL DEFAULT 100,
  used_count       INTEGER NOT NULL DEFAULT 0,
  active           INTEGER NOT NULL DEFAULT 1
);

CREATE INDEX IF NOT EXISTS idx_bookings_room_dates ON bookings(room_id, check_in, check_out);
CREATE INDEX IF NOT EXISTS idx_bookings_user ON bookings(user_id);
