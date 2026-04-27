# -*- coding: utf-8 -*-
"""Sinh tài liệu đặc tả yêu cầu & thiết kế (.docx) cho dự án LuxeHaven Booking."""
from docx import Document
from docx.shared import Pt, Cm, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.oxml.ns import qn
from docx.oxml import OxmlElement


def set_cell_bg(cell, color_hex):
    tc_pr = cell._tc.get_or_add_tcPr()
    shd = OxmlElement('w:shd')
    shd.set(qn('w:val'), 'clear')
    shd.set(qn('w:color'), 'auto')
    shd.set(qn('w:fill'), color_hex)
    tc_pr.append(shd)


def add_heading(doc, text, level=1):
    h = doc.add_heading(text, level=level)
    for run in h.runs:
        run.font.name = 'Calibri'
    return h


def add_para(doc, text, bold=False, italic=False, size=11):
    p = doc.add_paragraph()
    run = p.add_run(text)
    run.font.name = 'Calibri'
    run.font.size = Pt(size)
    run.bold = bold
    run.italic = italic
    return p


def add_code_block(doc, code, caption=None):
    if caption:
        p = doc.add_paragraph()
        r = p.add_run(caption)
        r.italic = True
        r.font.size = Pt(10)
    p = doc.add_paragraph()
    p.paragraph_format.left_indent = Cm(0.5)
    run = p.add_run(code)
    run.font.name = 'Consolas'
    run.font.size = Pt(9)


def add_table(doc, headers, rows, col_widths=None):
    table = doc.add_table(rows=1 + len(rows), cols=len(headers))
    table.style = 'Light Grid Accent 1'
    table.alignment = WD_TABLE_ALIGNMENT.CENTER
    hdr = table.rows[0].cells
    for i, h in enumerate(headers):
        hdr[i].text = ''
        p = hdr[i].paragraphs[0]
        run = p.add_run(h)
        run.bold = True
        run.font.size = Pt(10)
        set_cell_bg(hdr[i], '4F81BD')
        for r in p.runs:
            r.font.color.rgb = RGBColor(0xFF, 0xFF, 0xFF)
    for ri, row in enumerate(rows, start=1):
        cells = table.rows[ri].cells
        for ci, val in enumerate(row):
            cells[ci].text = ''
            p = cells[ci].paragraphs[0]
            run = p.add_run(str(val))
            run.font.size = Pt(10)
    if col_widths:
        for row in table.rows:
            for i, w in enumerate(col_widths):
                row.cells[i].width = Cm(w)
    return table


# ===== Tạo document =====
doc = Document()

style = doc.styles['Normal']
style.font.name = 'Calibri'
style.font.size = Pt(11)

# ===== TRANG BÌA =====
title = doc.add_paragraph()
title.alignment = WD_ALIGN_PARAGRAPH.CENTER
r = title.add_run('TRƯỜNG / KHOA CÔNG NGHỆ THÔNG TIN')
r.bold = True
r.font.size = Pt(13)

doc.add_paragraph()
doc.add_paragraph()
doc.add_paragraph()

t = doc.add_paragraph()
t.alignment = WD_ALIGN_PARAGRAPH.CENTER
r = t.add_run('TÀI LIỆU ĐẶC TẢ YÊU CẦU & THIẾT KẾ')
r.bold = True
r.font.size = Pt(20)

t = doc.add_paragraph()
t.alignment = WD_ALIGN_PARAGRAPH.CENTER
r = t.add_run('LuxeHaven – Hệ thống đặt phòng khách sạn')
r.bold = True
r.font.size = Pt(16)
r.font.color.rgb = RGBColor(0x1F, 0x49, 0x7D)

doc.add_paragraph()
doc.add_paragraph()

t = doc.add_paragraph()
t.alignment = WD_ALIGN_PARAGRAPH.CENTER
r = t.add_run('Phiên bản: 1.0   |   Ngôn ngữ: Tiếng Việt   |   Tháng 4/2026')
r.italic = True

doc.add_page_break()

# ===== MỤC LỤC =====
add_heading(doc, 'MỤC LỤC', level=1)
toc_items = [
    '1. Tổng quan dự án',
    '2. Yêu cầu chức năng (Functional Requirements)',
    '3. Yêu cầu phi chức năng (Non-Functional Requirements)',
    '4. Sơ đồ luồng dữ liệu (Data Flow Diagram)',
    '5. Sơ đồ Use Case',
    '6. Sơ đồ lớp (Class Diagram)',
    '7. Mô hình dữ liệu (Data Model)',
    '8. Thiết kế giao diện (Interface Design)',
    '9. Kế hoạch kiểm thử (Test Plan)',
]
for it in toc_items:
    doc.add_paragraph(it, style='List Number')
doc.add_page_break()

# ===== 1. TỔNG QUAN =====
add_heading(doc, '1. Tổng quan dự án', level=1)
add_para(doc,
         'LuxeHaven là hệ thống web cho phép khách hàng tìm kiếm và đặt phòng khách sạn trực tuyến, '
         'đồng thời cung cấp công cụ cho quản trị viên quản lý phòng và đơn đặt phòng. '
         'Sản phẩm hướng tới khách sạn quy mô nhỏ/vừa, cần một giải pháp đơn giản, dễ triển khai.')

add_heading(doc, '1.1 Công nghệ sử dụng', level=2)
tech = [
    ('Backend', 'Java 17, Spring Boot 4.0.6, Spring JDBC'),
    ('Frontend', 'Thymeleaf + Bootstrap 5 (CDN)'),
    ('CSDL', 'SQLite (file booking.db)'),
    ('Build tool', 'Maven (qua mvnw wrapper)'),
    ('Bảo mật', 'BCrypt (mật khẩu), HttpSession (phiên đăng nhập)'),
    ('Kiểm thử', 'JUnit 5 + Spring Boot Test'),
    ('IDE', 'IntelliJ / Eclipse / VS Code'),
]
add_table(doc, ['Thành phần', 'Công nghệ / Thư viện'], tech, [4, 11])

add_heading(doc, '1.2 Phạm vi (Scope)', level=2)
add_para(doc, 'Trong phạm vi:', bold=True)
for s in [
    'Quản lý tài khoản người dùng (đăng ký, đăng nhập, đăng xuất).',
    'Quản lý phòng: thêm, sửa, xoá, tìm kiếm.',
    'Đặt phòng, huỷ phòng, xem lịch sử đặt phòng.',
    'Thanh toán tượng trưng (chỉ cập nhật trạng thái PAID/UNPAID, không tích hợp cổng thanh toán thật).',
    'Quản trị viên xem & cập nhật trạng thái đơn đặt phòng.',
]:
    doc.add_paragraph(s, style='List Bullet')

add_para(doc, 'Ngoài phạm vi:', bold=True)
for s in [
    'Cổng thanh toán thật (VNPay, Momo, Stripe…).',
    'Email xác thực / khôi phục mật khẩu.',
    'Đa ngôn ngữ (chỉ có tiếng Việt trong v1).',
    'Quản lý nhân viên, ca làm việc, kho hàng.',
]:
    doc.add_paragraph(s, style='List Bullet')

doc.add_page_break()

# ===== 2. YÊU CẦU CHỨC NĂNG =====
add_heading(doc, '2. Yêu cầu chức năng (Functional Requirements)', level=1)
add_para(doc, 'Bảng liệt kê các chức năng chính của hệ thống, sắp xếp theo nhóm người dùng.')

fr = [
    ('FR-01', 'Đăng ký tài khoản', 'Khách (Guest)',
     'Người dùng điền username, password, họ tên, email, sđt → hệ thống tạo tài khoản mới với role = CUSTOMER. Mật khẩu được hash BCrypt trước khi lưu.'),
    ('FR-02', 'Đăng nhập', 'Tất cả',
     'Xác thực bằng username + password. Thành công → lưu user vào HttpSession và chuyển hướng về trang chủ.'),
    ('FR-03', 'Đăng xuất', 'Đã đăng nhập',
     'Huỷ session hiện tại và chuyển hướng về trang đăng nhập.'),
    ('FR-04', 'Xem danh sách phòng', 'Tất cả',
     'Hiển thị tất cả phòng đang AVAILABLE kèm hình ảnh / mô tả / giá / loại.'),
    ('FR-05', 'Tìm phòng theo ngày', 'Tất cả',
     'Lọc các phòng không có booking (status PENDING/CONFIRMED) trùng với khoảng [check-in, check-out] yêu cầu.'),
    ('FR-06', 'Lọc phòng theo loại', 'Tất cả',
     'Lọc theo STANDARD / DELUXE / SUITE.'),
    ('FR-07', 'Xem chi tiết phòng', 'Tất cả',
     'Xem mô tả đầy đủ, giá/đêm, sức chứa, trạng thái.'),
    ('FR-08', 'Đặt phòng', 'Customer',
     'Nhập check-in, check-out → hệ thống tính total_price = price_per_night × số_đêm → tạo booking PENDING.'),
    ('FR-09', 'Thanh toán tượng trưng', 'Customer (chủ booking)',
     'Bấm nút "Thanh toán" → đổi payment_status sang PAID, status sang CONFIRMED. Không tích hợp cổng thanh toán thật.'),
    ('FR-10', 'Xem lịch sử đặt phòng', 'Customer',
     'Liệt kê mọi booking của user hiện tại, sắp xếp mới nhất trước.'),
    ('FR-11', 'Huỷ đặt phòng', 'Customer (chủ booking)',
     'Cho phép huỷ khi status = PENDING hoặc CONFIRMED và check_in chưa diễn ra. Đổi status → CANCELLED.'),
    ('FR-12', 'Quản lý phòng (CRUD)', 'Admin',
     'Tạo/sửa/xoá phòng. Không cho xoá nếu phòng đang có booking PENDING/CONFIRMED.'),
    ('FR-13', 'Xem tất cả booking', 'Admin',
     'Liệt kê tất cả booking, lọc theo trạng thái, theo user.'),
    ('FR-14', 'Cập nhật trạng thái booking', 'Admin',
     'Đổi status: PENDING → CONFIRMED, CONFIRMED → COMPLETED, hoặc CANCELLED.'),
]
add_table(doc, ['ID', 'Tên chức năng', 'Vai trò', 'Mô tả'], fr, [1.5, 4, 3, 8])

doc.add_page_break()

# ===== 3. YÊU CẦU PHI CHỨC NĂNG =====
add_heading(doc, '3. Yêu cầu phi chức năng (Non-Functional Requirements)', level=1)
nfr = [
    ('NFR-01', 'Hiệu năng', 'Trang danh sách phòng tải dưới 2 giây với 100 phòng.'),
    ('NFR-02', 'Bảo mật', 'Mật khẩu hash BCrypt (cost factor 10). Không lưu mật khẩu dạng rõ.'),
    ('NFR-03', 'Bảo mật', 'Phân quyền: customer không truy cập được trang /admin/**.'),
    ('NFR-04', 'Bảo mật', 'Session timeout 30 phút không hoạt động.'),
    ('NFR-05', 'Khả dụng', 'Giao diện responsive (Bootstrap 5), dùng được trên mobile/tablet/desktop.'),
    ('NFR-06', 'Tin cậy', 'Validate đầu vào: check_out > check_in, ngày không quá khứ, không trùng booking đã có.'),
    ('NFR-07', 'Tin cậy', 'Transaction database cho thao tác đặt/huỷ phòng.'),
    ('NFR-08', 'Bảo trì', 'Code phân lớp rõ: model / repository / service / controller. Coverage mục tiêu ≥ 60%.'),
    ('NFR-09', 'Quốc tế hoá', 'Toàn bộ UI tiếng Việt (file thông báo tập trung để mở rộng).'),
    ('NFR-10', 'Tương thích', 'Chạy được trên Windows / Linux / macOS với Java 17+.'),
    ('NFR-11', 'Triển khai', 'Build chỉ cần lệnh ./mvnw spring-boot:run, không cần cài đặt thêm DB server.'),
]
add_table(doc, ['ID', 'Loại', 'Mô tả'], nfr, [1.5, 3, 12])

doc.add_page_break()

# ===== 4. DFD =====
add_heading(doc, '4. Sơ đồ luồng dữ liệu (Data Flow Diagram)', level=1)
add_heading(doc, '4.1 DFD mức 0 (Context Diagram)', level=2)
add_para(doc,
         'Ở mức context, hệ thống được xem là một khối đen nhận input từ Khách hàng và Quản trị viên, '
         'trả về thông tin phòng & xác nhận đặt phòng.')

add_code_block(doc, """
   +-----------+       yêu cầu đặt phòng        +----------------------+
   |           | -----------------------------> |                      |
   |  Khách    |                                |                      |
   |  hàng     | <----------------------------- |   Hệ thống           |
   |           |       xác nhận / hoá đơn       |   LuxeHaven Booking  |
   +-----------+                                |                      |
                                                |                      |
   +-----------+   thao tác quản lý             |                      |
   |  Quản trị | -----------------------------> |                      |
   |  viên     | <----------------------------- |                      |
   +-----------+   báo cáo / danh sách          +----------------------+
""", caption='Hình 4.1 – DFD mức 0')

add_heading(doc, '4.2 DFD mức 1', level=2)
add_para(doc, 'Phân rã hệ thống thành 4 quy trình chính và 3 kho dữ liệu (users, rooms, bookings).')

add_code_block(doc, """
                          +-------------------+
   Khách ----đăng ký/đn-->| 1. Quản lý        |---+
                          |    tài khoản      |   |
                          +-------------------+   |
                                  |               v
                                  |          [D1: users]
                                  v
                          +-------------------+
   Khách ----tìm phòng-->| 2. Tìm kiếm &     |<--+
                          |    xem phòng      |   |
                          +-------------------+   |
                                  |               v
                                  |          [D2: rooms]
                                  v
                          +-------------------+
   Khách ----đặt/huỷ----->| 3. Đặt phòng &    |---+
                          |    thanh toán     |   |
                          +-------------------+   v
                                  |          [D3: bookings]
                                  v
                          +-------------------+
   Admin ---CRUD/xem ---->| 4. Quản trị       |<--+
                          |    hệ thống       |
                          +-------------------+
""", caption='Hình 4.2 – DFD mức 1')

doc.add_page_break()

# ===== 5. USE CASE =====
add_heading(doc, '5. Sơ đồ Use Case', level=1)
add_heading(doc, '5.1 Các tác nhân (Actors)', level=2)
actors = [
    ('Guest', 'Người dùng chưa đăng nhập. Xem được danh sách/chi tiết phòng, tìm kiếm, đăng ký tài khoản.'),
    ('Customer', 'Người dùng đã đăng nhập với role CUSTOMER. Bao gồm mọi quyền của Guest + đặt phòng, huỷ phòng, xem lịch sử, thanh toán.'),
    ('Admin', 'Người dùng có role ADMIN. Quản lý phòng (CRUD), xem và cập nhật trạng thái mọi đơn đặt phòng.'),
]
add_table(doc, ['Actor', 'Mô tả'], actors, [3, 12])

add_heading(doc, '5.2 Danh sách Use Case', level=2)
ucs = [
    ('UC-01', 'Đăng ký tài khoản', 'Guest', 'FR-01'),
    ('UC-02', 'Đăng nhập', 'Guest, Customer, Admin', 'FR-02'),
    ('UC-03', 'Đăng xuất', 'Customer, Admin', 'FR-03'),
    ('UC-04', 'Xem danh sách phòng', 'Guest, Customer, Admin', 'FR-04'),
    ('UC-05', 'Tìm phòng theo ngày/loại', 'Guest, Customer, Admin', 'FR-05, FR-06'),
    ('UC-06', 'Xem chi tiết phòng', 'Guest, Customer, Admin', 'FR-07'),
    ('UC-07', 'Đặt phòng', 'Customer', 'FR-08'),
    ('UC-08', 'Thanh toán tượng trưng', 'Customer', 'FR-09'),
    ('UC-09', 'Xem lịch sử đặt phòng', 'Customer', 'FR-10'),
    ('UC-10', 'Huỷ đặt phòng', 'Customer', 'FR-11'),
    ('UC-11', 'Quản lý phòng (CRUD)', 'Admin', 'FR-12'),
    ('UC-12', 'Xem tất cả booking', 'Admin', 'FR-13'),
    ('UC-13', 'Cập nhật trạng thái booking', 'Admin', 'FR-14'),
]
add_table(doc, ['ID', 'Use Case', 'Actor', 'FR liên quan'], ucs, [1.5, 5, 4.5, 4])

add_heading(doc, '5.3 Sơ đồ Use Case (mô tả văn bản)', level=2)
add_code_block(doc, """
                +--------------------------------------------------+
                |                Hệ thống LuxeHaven                |
                |                                                  |
                |  ( UC-01 Đăng ký )      ( UC-04 Xem phòng )      |
                |  ( UC-02 Đăng nhập )    ( UC-05 Tìm phòng )      |
                |  ( UC-03 Đăng xuất )    ( UC-06 Chi tiết )       |
                |                                                  |
                |  ( UC-07 Đặt phòng )    ( UC-11 CRUD phòng )     |
                |  ( UC-08 Thanh toán )   ( UC-12 Xem booking )    |
                |  ( UC-09 Lịch sử )      ( UC-13 Cập nhật tt )    |
                |  ( UC-10 Huỷ phòng )                             |
                +----^-------------------------^-------------------+
                     |                         |
                     |                         |
        +------------+---------+      +--------+--------+
        |       Guest          |      |      Admin      |
        +-------^--------------+      +-----------------+
                | <<extends>>
        +-------+--------------+
        |     Customer         |
        +----------------------+
""", caption='Hình 5.1 – Sơ đồ Use Case')

add_heading(doc, '5.4 Đặc tả chi tiết UC-07 (Đặt phòng)', level=2)
uc_detail = [
    ('Tên', 'UC-07 Đặt phòng'),
    ('Actor chính', 'Customer'),
    ('Tiền điều kiện', 'User đã đăng nhập với role CUSTOMER. Phòng được chọn có status AVAILABLE.'),
    ('Hậu điều kiện', 'Một đơn booking mới với status PENDING được tạo.'),
    ('Luồng chính',
     '1. Customer chọn một phòng từ danh sách.\n'
     '2. Bấm "Đặt ngay".\n'
     '3. Hệ thống hiện form check-in/check-out.\n'
     '4. Customer nhập ngày và xác nhận.\n'
     '5. Hệ thống kiểm tra phòng trống, tính total_price.\n'
     '6. Hệ thống tạo booking PENDING và chuyển hướng tới trang my-bookings.'),
    ('Luồng phụ (5a)', 'Phòng đã có booking trùng ngày → báo lỗi "Phòng đã được đặt trong khoảng này" → quay lại bước 3.'),
    ('Luồng phụ (4a)', 'Ngày không hợp lệ (check_out ≤ check_in) → báo lỗi → quay lại bước 3.'),
]
add_table(doc, ['Trường', 'Nội dung'], uc_detail, [3.5, 11.5])

doc.add_page_break()

# ===== 6. CLASS DIAGRAM =====
add_heading(doc, '6. Sơ đồ lớp (Class Diagram)', level=1)
add_para(doc,
         'Hệ thống gồm 3 entity chính: User, Room, Booking. Các enum đi kèm mô tả '
         'role, loại phòng, trạng thái phòng, trạng thái booking, trạng thái thanh toán.')

add_code_block(doc, """
+-----------------------------+        +------------------------------+
|           User              |        |            Room              |
+-----------------------------+        +------------------------------+
| - id : Long                 |        | - id : Long                  |
| - username : String         |        | - roomNumber : String        |
| - password : String         |        | - type : RoomType            |
| - fullName : String         |        | - pricePerNight : double     |
| - email : String            |        | - capacity : int             |
| - phone : String            |        | - description : String       |
| - role : Role               |        | - status : RoomStatus        |
| - createdAt : LocalDateTime |        +------------------------------+
+-----------------------------+                       |
            |                                         |
            | 1                                     1 |
            |                                         |
            |          *  +-----------------------+  *|
            +------------>|        Booking        |<--+
                          +-----------------------+
                          | - id : Long           |
                          | - userId : Long       |
                          | - roomId : Long       |
                          | - checkIn : LocalDate |
                          | - checkOut: LocalDate |
                          | - totalPrice : double |
                          | - status:BookingStatus|
                          | - paymentStatus:PayS  |
                          | - createdAt : LDT     |
                          +-----------------------+

Enums:
  Role           = { CUSTOMER, ADMIN }
  RoomType       = { STANDARD, DELUXE, SUITE }
  RoomStatus     = { AVAILABLE, MAINTENANCE }
  BookingStatus  = { PENDING, CONFIRMED, CANCELLED, COMPLETED }
  PaymentStatus  = { UNPAID, PAID }
""", caption='Hình 6.1 – Class Diagram')

add_heading(doc, '6.1 Quan hệ', level=2)
rel = [
    ('User – Booking', '1 .. *', 'Một user có thể có nhiều booking; mỗi booking thuộc đúng 1 user.'),
    ('Room – Booking', '1 .. *', 'Một phòng có thể được đặt nhiều lần (khác thời gian); mỗi booking thuộc 1 phòng.'),
]
add_table(doc, ['Quan hệ', 'Bội số', 'Ý nghĩa'], rel, [4, 2, 9])

doc.add_page_break()

# ===== 7. DATA MODEL =====
add_heading(doc, '7. Mô hình dữ liệu (Data Model)', level=1)
add_para(doc, 'Sử dụng SQLite. Có 3 bảng chính: users, rooms, bookings.')

add_heading(doc, '7.1 Bảng users', level=2)
users_cols = [
    ('id', 'INTEGER', 'PK, AUTOINCREMENT', 'Khoá chính'),
    ('username', 'TEXT', 'NOT NULL, UNIQUE', 'Tên đăng nhập'),
    ('password', 'TEXT', 'NOT NULL', 'Mật khẩu hash BCrypt'),
    ('full_name', 'TEXT', 'NOT NULL', 'Họ và tên'),
    ('email', 'TEXT', 'NOT NULL, UNIQUE', 'Địa chỉ email'),
    ('phone', 'TEXT', '', 'Số điện thoại'),
    ('role', 'TEXT', 'NOT NULL, DEFAULT CUSTOMER', 'CUSTOMER | ADMIN'),
    ('created_at', 'TEXT', 'NOT NULL, DEFAULT CURRENT_TIMESTAMP', 'Thời điểm tạo'),
]
add_table(doc, ['Cột', 'Kiểu', 'Ràng buộc', 'Mô tả'], users_cols, [3, 2, 5, 5])

add_heading(doc, '7.2 Bảng rooms', level=2)
rooms_cols = [
    ('id', 'INTEGER', 'PK, AUTOINCREMENT', 'Khoá chính'),
    ('room_number', 'TEXT', 'NOT NULL, UNIQUE', 'Số phòng (vd: 101, 202)'),
    ('type', 'TEXT', 'NOT NULL', 'STANDARD | DELUXE | SUITE'),
    ('price_per_night', 'REAL', 'NOT NULL, > 0', 'Giá/đêm (VND)'),
    ('capacity', 'INTEGER', 'NOT NULL, > 0', 'Số người tối đa'),
    ('description', 'TEXT', '', 'Mô tả phòng'),
    ('status', 'TEXT', 'NOT NULL, DEFAULT AVAILABLE', 'AVAILABLE | MAINTENANCE'),
]
add_table(doc, ['Cột', 'Kiểu', 'Ràng buộc', 'Mô tả'], rooms_cols, [3, 2, 5, 5])

add_heading(doc, '7.3 Bảng bookings', level=2)
bk_cols = [
    ('id', 'INTEGER', 'PK, AUTOINCREMENT', 'Khoá chính'),
    ('user_id', 'INTEGER', 'NOT NULL, FK → users(id)', 'Người đặt'),
    ('room_id', 'INTEGER', 'NOT NULL, FK → rooms(id)', 'Phòng được đặt'),
    ('check_in', 'DATE', 'NOT NULL', 'Ngày nhận phòng'),
    ('check_out', 'DATE', 'NOT NULL, > check_in', 'Ngày trả phòng'),
    ('total_price', 'REAL', 'NOT NULL', 'Tổng tiền'),
    ('status', 'TEXT', 'NOT NULL, DEFAULT PENDING', 'PENDING | CONFIRMED | CANCELLED | COMPLETED'),
    ('payment_status', 'TEXT', 'NOT NULL, DEFAULT UNPAID', 'UNPAID | PAID'),
    ('created_at', 'TEXT', 'NOT NULL, DEFAULT CURRENT_TIMESTAMP', 'Thời điểm đặt'),
]
add_table(doc, ['Cột', 'Kiểu', 'Ràng buộc', 'Mô tả'], bk_cols, [3, 2, 5, 5])

add_heading(doc, '7.4 Câu lệnh DDL đầy đủ', level=2)
add_code_block(doc, """CREATE TABLE IF NOT EXISTS users (
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
  status          TEXT    NOT NULL DEFAULT 'AVAILABLE'
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
  created_at      TEXT    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (room_id) REFERENCES rooms(id),
  CHECK (check_out > check_in)
);
""")

doc.add_page_break()

# ===== 8. INTERFACE DESIGN =====
add_heading(doc, '8. Thiết kế giao diện (Interface Design)', level=1)
add_para(doc, 'Giao diện web được render bằng Thymeleaf, dùng Bootstrap 5 cho responsive layout.')

pages = [
    ('/', 'Trang chủ',
     'Hiện banner, form tìm kiếm (check-in, check-out, loại phòng) và danh sách phòng nổi bật.'),
    ('/login', 'Đăng nhập',
     'Form 2 trường: username, password. Có link sang trang đăng ký.'),
    ('/register', 'Đăng ký',
     'Form: username, password, full_name, email, phone. Validate phía client + server.'),
    ('/rooms', 'Danh sách phòng',
     'Card grid 3 cột (responsive). Mỗi card: ảnh, tên phòng, loại, giá/đêm, nút "Xem chi tiết".'),
    ('/rooms/{id}', 'Chi tiết phòng',
     'Ảnh lớn, mô tả đầy đủ, sức chứa. Form chọn ngày check-in/out + nút "Đặt ngay".'),
    ('/bookings/new?roomId=X', 'Form đặt phòng',
     'Hiển thị tóm tắt phòng, ngày, tính trước total_price, nút "Xác nhận đặt".'),
    ('/my-bookings', 'Lịch sử đặt phòng',
     'Bảng liệt kê booking của user: mã, phòng, ngày, tổng tiền, trạng thái, nút "Thanh toán" / "Huỷ".'),
    ('/admin', 'Dashboard Admin',
     'Tóm tắt: tổng số phòng, tổng booking, doanh thu tháng.'),
    ('/admin/rooms', 'Quản lý phòng',
     'Bảng phòng + nút Thêm/Sửa/Xoá.'),
    ('/admin/rooms/new', 'Form thêm/sửa phòng',
     'Các trường của bảng rooms.'),
    ('/admin/bookings', 'Quản lý booking',
     'Bảng booking, lọc theo trạng thái, nút Confirm/Complete/Cancel.'),
]
add_table(doc, ['URL', 'Trang', 'Mô tả layout'], pages, [4.5, 4, 8])

add_heading(doc, '8.1 Quy ước giao diện', level=2)
for s in [
    'Màu chủ đạo: xanh navy (#1F497D) cho header, vàng nhạt cho nút CTA.',
    'Font: hệ thống (Bootstrap default) – Calibri/Segoe UI trên Windows.',
    'Mobile first: các bảng lớn trên mobile chuyển sang dạng thẻ (card).',
    'Thông báo lỗi: alert-danger; thông báo thành công: alert-success.',
    'Format ngày: dd/MM/yyyy. Format tiền: 1.234.567 VND.',
]:
    doc.add_paragraph(s, style='List Bullet')

doc.add_page_break()

# ===== 9. TEST PLAN =====
add_heading(doc, '9. Kế hoạch kiểm thử (Test Plan)', level=1)
add_para(doc, 'Sử dụng JUnit 5 + Spring Boot Test cho unit/integration test. '
              'Bộ test case được trình bày dưới đây, dự kiến lưu file Excel riêng (test-cases.xlsx) cho team QA.')

tc = [
    ('TC-01', 'Đăng ký thành công', 'POST /register với dữ liệu hợp lệ', 'Tạo user mới, redirect /login', 'High'),
    ('TC-02', 'Đăng ký username trùng', 'POST /register với username đã tồn tại', 'Báo lỗi "Username đã tồn tại"', 'High'),
    ('TC-03', 'Đăng nhập đúng', 'POST /login đúng username/password', 'Redirect /, session có user', 'High'),
    ('TC-04', 'Đăng nhập sai mật khẩu', 'POST /login sai password', 'Báo lỗi "Sai thông tin"', 'High'),
    ('TC-05', 'Xem danh sách phòng', 'GET /rooms', 'Trả về danh sách phòng AVAILABLE', 'Medium'),
    ('TC-06', 'Tìm phòng theo ngày', 'GET /rooms?checkIn=…&checkOut=…', 'Loại bỏ phòng đã có booking trùng', 'High'),
    ('TC-07', 'Đặt phòng thành công', 'POST /bookings với ngày hợp lệ', 'Tạo booking PENDING, total_price đúng', 'High'),
    ('TC-08', 'Đặt phòng trùng lịch', 'POST /bookings với phòng đã có booking trùng', 'Báo lỗi, không tạo booking', 'High'),
    ('TC-09', 'Đặt phòng check_out ≤ check_in', 'POST /bookings ngày không hợp lệ', 'Báo lỗi validate', 'High'),
    ('TC-10', 'Thanh toán tượng trưng', 'POST /bookings/{id}/pay', 'payment_status=PAID, status=CONFIRMED', 'Medium'),
    ('TC-11', 'Huỷ booking', 'POST /bookings/{id}/cancel', 'status=CANCELLED', 'Medium'),
    ('TC-12', 'Customer truy cập /admin', 'GET /admin khi role=CUSTOMER', 'HTTP 403 Forbidden', 'High'),
    ('TC-13', 'Admin thêm phòng', 'POST /admin/rooms với dữ liệu hợp lệ', 'Tạo phòng mới', 'Medium'),
    ('TC-14', 'Admin xoá phòng có booking', 'DELETE /admin/rooms/{id} có booking PENDING', 'Báo lỗi, không xoá', 'Medium'),
    ('TC-15', 'Admin đổi trạng thái booking', 'POST /admin/bookings/{id}/status', 'Status được cập nhật', 'Low'),
]
add_table(doc, ['ID', 'Tên', 'Bước thực hiện', 'Kết quả mong đợi', 'Mức độ'], tc, [1.5, 4, 4.5, 4.5, 1.5])

add_heading(doc, '9.1 Quản lý bug', level=2)
add_para(doc,
         'Dự án sử dụng GitHub Issues làm bug tracker (hoặc Jira / Mantis nếu yêu cầu). '
         'Mỗi bug được tạo issue với nhãn: bug, severity (critical/major/minor), '
         'kèm bước tái hiện và ảnh chụp màn hình.')

# ===== LƯU FILE =====
out = r'd:/booking/docs/yeu-cau-thiet-ke.docx'
doc.save(out)
print('Saved: ' + out)
