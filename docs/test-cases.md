# Bộ Test Case — LuxeHaven Booking

> File này được trình bày dạng Markdown để dễ đọc trên GitHub. Có thể copy bảng dưới đây paste vào Excel để có file `test-cases.xlsx` nộp kèm.

## 1. Quy ước

| Mức độ | Ý nghĩa |
|---|---|
| High | Chức năng chính, ảnh hưởng dòng tiền hoặc bảo mật |
| Medium | Chức năng phụ trợ |
| Low | Chỉ ảnh hưởng UX, không ảnh hưởng dữ liệu |

| Trạng thái | Ý nghĩa |
|---|---|
| Pass | Kết quả khớp expected |
| Fail | Sai khác — tạo bug ticket |
| Blocked | Không thể test do phụ thuộc |

## 2. Test cases

| ID | Module | Tên | Tiền điều kiện | Bước thực hiện | Kết quả mong đợi | Mức độ | Trạng thái |
|----|--------|-----|---------------|----------------|------------------|--------|-----------|
| TC-01 | Auth | Đăng ký thành công | Chưa có tài khoản | 1. Vào /register<br>2. Điền username/pass/email/họ tên hợp lệ<br>3. Submit | Tạo user mới, redirect /login với thông báo thành công | High | |
| TC-02 | Auth | Đăng ký username đã tồn tại | Đã có user `admin` | 1. /register với username=`admin` | Lỗi "Tên đăng nhập đã tồn tại" | High | |
| TC-03 | Auth | Đăng ký mật khẩu < 6 ký tự | – | 1. /register password=`abc` | Lỗi "Mật khẩu phải có ít nhất 6 ký tự" | Medium | |
| TC-04 | Auth | Đăng ký email không hợp lệ | – | 1. /register email=`abc` | Lỗi "Email không hợp lệ" | Medium | |
| TC-05 | Auth | Đăng nhập đúng | Có user `customer1/pass123` | 1. /login với customer1/pass123 | Redirect /, navbar hiện tên user | High | |
| TC-06 | Auth | Đăng nhập sai password | Có user customer1 | 1. /login với customer1/wrong | Báo lỗi "Sai tên đăng nhập hoặc mật khẩu" | High | |
| TC-07 | Auth | Đăng xuất | Đã đăng nhập | 1. Click "Đăng xuất" | Session bị huỷ, redirect /login | Medium | |
| TC-08 | Room | Xem danh sách phòng | Có ≥ 1 phòng AVAILABLE | 1. GET /rooms | Hiển thị tất cả phòng AVAILABLE | High | |
| TC-09 | Room | Tìm phòng theo ngày | Phòng 101 đã có booking 1/5–3/5 | 1. GET /rooms?checkIn=2026-05-01&checkOut=2026-05-03 | Phòng 101 không xuất hiện | High | |
| TC-10 | Room | Lọc theo loại phòng | Có cả 3 loại | 1. GET /rooms?type=SUITE | Chỉ hiện phòng SUITE | Medium | |
| TC-11 | Room | Xem chi tiết phòng | – | 1. GET /rooms/{id} hợp lệ | Hiện đầy đủ thông tin phòng + nút "Đặt phòng ngay" | High | |
| TC-12 | Booking | Đặt phòng thành công | Đã login customer | 1. /rooms/{id} → "Đặt ngay"<br>2. Chọn check_in, check_out hợp lệ<br>3. Submit | Tạo booking PENDING, total_price = nights × price, redirect /my-bookings | High | |
| TC-13 | Booking | Đặt phòng trùng lịch | Phòng đã có booking PENDING/CONFIRMED | 1. Đặt lại phòng đó với khoảng giao | Lỗi "Phòng đã được đặt trong khoảng thời gian này" | High | |
| TC-14 | Booking | check_out ≤ check_in | – | 1. Form đặt phòng nhập ngày sai | Lỗi "Ngày trả phòng phải sau ngày nhận phòng" | High | |
| TC-15 | Booking | check_in trong quá khứ | – | 1. Form đặt phòng check_in = hôm qua | Lỗi "Ngày nhận phòng không được ở quá khứ" | Medium | |
| TC-16 | Booking | Thanh toán tượng trưng | Booking PENDING của tôi | 1. /my-bookings → "Thanh toán" | payment_status=PAID, status=CONFIRMED | Medium | |
| TC-17 | Booking | Huỷ booking | Booking PENDING của tôi | 1. /my-bookings → "Huỷ" → confirm | status=CANCELLED | Medium | |
| TC-18 | Booking | Huỷ booking đã hoàn tất | Booking COMPLETED | 1. Cố huỷ booking đã COMPLETED | Lỗi "đã hoàn tất, không thể huỷ" | Low | |
| TC-19 | Booking | Customer thanh toán booking của user khác | Login customer1, biết id booking của customer2 | 1. POST /bookings/{otherId}/pay | Lỗi "Bạn không có quyền thao tác" | High | |
| TC-20 | Auth | Customer truy cập /admin | Login với role CUSTOMER | 1. GET /admin | HTTP 403 Forbidden | High | |
| TC-21 | Auth | Truy cập /my-bookings không đăng nhập | Chưa login | 1. GET /my-bookings | Redirect /login?redirect=/my-bookings | Medium | |
| TC-22 | Admin Room | Thêm phòng mới | Login admin | 1. /admin/rooms/new<br>2. Điền số phòng mới, type, price > 0, capacity > 0<br>3. Submit | Phòng mới xuất hiện trong /admin/rooms | Medium | |
| TC-23 | Admin Room | Thêm phòng số trùng | Đã có phòng 101 | 1. Thêm phòng số 101 | Lỗi "Số phòng đã tồn tại" | Medium | |
| TC-24 | Admin Room | Sửa phòng | Login admin | 1. /admin/rooms → Sửa<br>2. Đổi giá<br>3. Submit | Giá mới được lưu | Medium | |
| TC-25 | Admin Room | Xoá phòng có booking | Phòng X có booking PENDING | 1. /admin/rooms → Xoá phòng X | Lỗi "không thể xoá phòng đang có booking" | Medium | |
| TC-26 | Admin Room | Xoá phòng không có booking | Phòng Y không có booking | 1. Xoá phòng Y | Phòng Y bị xoá | Low | |
| TC-27 | Admin Booking | Xem tất cả booking | Login admin | 1. GET /admin/bookings | Bảng liệt kê tất cả booking | Medium | |
| TC-28 | Admin Booking | Lọc theo trạng thái | Có booking ở nhiều trạng thái | 1. /admin/bookings?status=PENDING | Chỉ hiện booking PENDING | Low | |
| TC-29 | Admin Booking | Cập nhật trạng thái | Booking PENDING | 1. Đổi status sang CONFIRMED → submit | status được cập nhật | Low | |
| TC-30 | Performance | Trang danh sách phòng < 2s | 100 phòng trong DB | 1. GET /rooms, đo thời gian response | < 2 giây | Low | |

## 3. Quản lý bug

Mỗi bug được tạo dưới dạng GitHub Issue với template:

```markdown
**Mô tả lỗi:**
**Bước tái hiện:**
1.
2.
3.
**Kết quả thực tế:**
**Kết quả mong đợi:**
**Severity:** critical / major / minor
**Môi trường:** OS, Java version, browser
**Screenshot:** (đính kèm)
```

Nhãn (label):
- `bug` — lỗi xác nhận
- `enhancement` — đề xuất cải tiến
- `severity:critical` / `severity:major` / `severity:minor`
- `module:auth` / `module:booking` / `module:admin`

## 4. Unit test tự động

Đã có sẵn dưới `src/test/java/com/luxehaven/booking/`:

| File | Số test | Mô tả |
|------|---------|-------|
| `BookingApplicationTests.java` | 1 | Spring context loads |
| `service/AuthServiceTest.java` | 5 | Đăng ký, đăng nhập, hash mật khẩu |
| `service/BookingServiceTest.java` | 6 | Tính tổng tiền, kiểm tra trùng lịch, thanh toán, huỷ |

Chạy: `./mvnw test`
