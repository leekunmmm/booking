# LuxeHaven Booking — Hướng dẫn chạy

## 1. Yêu cầu hệ thống

| Thành phần | Phiên bản tối thiểu | Đã test |
|---|---|---|
| Java JDK | 17 | 21.0.10 (Eclipse Adoptium) |
| Maven | (dùng `mvnw` đính kèm, không cần cài) | – |
| OS | Windows / Linux / macOS | Windows 11 |
| Trình duyệt | Bất kỳ trình duyệt hiện đại | Chrome / Edge / Firefox |

> **Lưu ý:** Máy bạn đang có `Java 8` là default trong PATH. JDK 21 đã cài tại `C:\Program Files\Eclipse Adoptium\jdk-21.0.10.7-hotspot\` và `JAVA_HOME` đã trỏ đúng → Maven wrapper sẽ tự dùng JDK 21.

## 2. Cấu trúc dự án

```
booking/
├── docs/
│   ├── yeu-cau-thiet-ke.docx     ← Tài liệu Stage 1 + 2 (yêu cầu / thiết kế)
│   ├── test-cases.md             ← Bảng test case
│   └── generate_docx.py          ← Script sinh lại file .docx
├── src/main/java/com/luxehaven/booking/
│   ├── BookingApplication.java   ← Entry point
│   ├── model/                    ← User, Room, Booking + 5 enum
│   ├── repository/               ← 3 JDBC repositories
│   ├── service/                  ← AuthService, RoomService, BookingService
│   ├── controller/               ← Home / Auth / Booking / Admin / GlobalAdvice
│   ├── dto/                      ← Form binding (Register, Login, Booking, Room)
│   ├── config/                   ← SecurityConfig (BCrypt), AuthInterceptor, WebConfig, DataSeeder
│   └── exception/                ← BusinessException
├── src/main/resources/
│   ├── application.properties    ← Cấu hình
│   ├── schema.sql                ← DDL
│   ├── data.sql                  ← Seed phòng mẫu
│   └── templates/                ← Thymeleaf views (HTML tiếng Việt)
├── src/test/java/...             ← 12 unit test
├── pom.xml                       ← Maven build
└── booking.db                    ← SQLite (sinh khi chạy lần đầu)
```

## 3. Chạy ứng dụng (3 bước)

### Bước 1 — Mở terminal tại thư mục dự án

```bash
cd d:/booking
```

### Bước 2 — Build & chạy

**Trên Windows (CMD/PowerShell):**
```cmd
mvnw.cmd spring-boot:run
```

**Trên Git Bash / Linux / macOS:**
```bash
./mvnw spring-boot:run
```

Lần đầu Maven sẽ tải ~150 MB dependency, hãy đợi 2–5 phút. Khi thấy dòng:

```
Started BookingApplication in X.XXX seconds
Tomcat started on port 8080
```

→ ứng dụng đã sẵn sàng.

### Bước 3 — Mở trình duyệt

Truy cập **http://localhost:8080**

Tài khoản demo (đã được seed tự động):

| Vai trò | Username | Password |
|---|---|---|
| Quản trị viên | `admin` | `admin123` |
| Khách hàng | `customer1` | `pass123` |

## 4. Kịch bản demo nhanh

1. **Mở** http://localhost:8080 → trang chủ hiện 6 phòng mẫu.
2. **Click** "Xem chi tiết" một phòng bất kỳ.
3. **Click** "Đặt phòng ngay" → chuyển đến /login (vì chưa đăng nhập).
4. **Đăng nhập** với `customer1 / pass123`.
5. **Tự động** quay lại form đặt phòng → chọn ngày → "Xác nhận đặt phòng".
6. **Vào** "Đặt phòng của tôi" → bấm "Thanh toán" → trạng thái đổi sang `CONFIRMED / PAID`.
7. **Đăng xuất** → đăng nhập lại với `admin / admin123`.
8. **Vào** "Quản trị" → xem dashboard, thử thêm/sửa/xoá phòng và xem booking vừa tạo.

## 5. Chạy test

```bash
./mvnw test
```

Kỳ vọng: `Tests run: 12, Failures: 0, Errors: 0`.

## 6. Reset dữ liệu

Tắt server (Ctrl+C) rồi xoá file SQLite:

```bash
rm booking.db
# Trên Windows CMD: del booking.db
```

Lần chạy kế tiếp `schema.sql` + `data.sql` + `DataSeeder` sẽ tạo lại từ đầu.

## 7. Các URL quan trọng

| URL | Mô tả | Quyền |
|---|---|---|
| `/` | Trang chủ + tìm kiếm | Public |
| `/rooms` | Danh sách & lọc phòng | Public |
| `/rooms/{id}` | Chi tiết phòng | Public |
| `/login`, `/register` | Đăng nhập / đăng ký | Public |
| `/bookings/new?roomId=…` | Form đặt phòng | Customer |
| `/my-bookings` | Lịch sử đặt phòng | Customer |
| `/admin` | Dashboard quản trị | Admin |
| `/admin/rooms` | Quản lý phòng | Admin |
| `/admin/bookings` | Quản lý đặt phòng | Admin |

## 8. Trouble-shooting

| Triệu chứng | Cách xử lý |
|---|---|
| `Port 8080 already in use` | Đổi port: thêm `--server.port=8081` vào lệnh chạy hoặc sửa `application.properties`. |
| `JAVA_HOME not set` | Set biến môi trường: `set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.10.7-hotspot` (CMD) |
| Trang trắng / 500 | Xem log console, chú ý dòng `ERROR`. |
| Không gõ được tiếng Việt khi đăng ký | Đảm bảo browser dùng UTF-8 (mặc định OK). Nếu vẫn lỗi, thêm `?form-charset=UTF-8` vào URL. |
| Muốn xoá toàn bộ data | Xoá file `booking.db` rồi chạy lại. |

## 9. Đóng gói (tuỳ chọn)

```bash
./mvnw clean package
java -jar target/booking-0.0.1-SNAPSHOT.jar
```

File JAR tự chứa, có thể copy sang máy khác chỉ cần Java 17+.

## 10. Tham khảo tài liệu

- **Tài liệu yêu cầu & thiết kế** (Stage 1 + 2): [docs/yeu-cau-thiet-ke.docx](docs/yeu-cau-thiet-ke.docx)
- **Bộ test case** (Stage 4): [docs/test-cases.md](docs/test-cases.md)
- **Sinh lại file .docx**: `cd docs && python generate_docx.py`
