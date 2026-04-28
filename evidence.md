# Evidence of Tool Usage and Task Assignment

## Project: LuxeHaven Hotel Booking System

---

## 1. Team Members & Task Assignment

| Thành viên          | MSSV         | Phân công công việc                                                                                                                                            |
| ------------------- | ------------ | -------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Bùi Thanh Hậu       | 2251120152   | Lập trình backend (Spring Boot, JDBC, services, controllers), thiết kế cơ sở dữ liệu, triển khai Docker, đẩy lên github, hỗ trợ viết (Use Case, Class Diagram) |
| Nguyễn Lê Duy Hoàng | 2251120013 | Soạn thảo tài liệu yêu cầu và thiết kế (Word), lập trình AuthController (đăng nhập, đăng ký, hồ sơ cá nhân), xây dựng các lớp DTO, viết test cases thủ công |
| Phạm Minh Duy       | 068206002286 | Thiết kế giao diện frontend (Thymeleaf templates, Bootstrap), trang admin                                                                                      |
| Kiều Duy Hưng       | 089205010462 | Lập trình tính năng đặt phòng, thanh toán, quản lý voucher                                                                                                     |
| Đỗ Văn Kịp          | 051206010713 | Vẽ sơ đồ UML (Use Case, Class Diagram, ERD, DFD), viết báo cáo                                                                                                 |
| Lê Đức Thiện        | 066205014318 | Kiểm thử (chạy test cases thủ công, JUnit), viết evidence                                                                                                      |

---

## 2. Tools Used

| Tool                           | Purpose                              | Evidence                                               |
| ------------------------------ | ------------------------------------ | ------------------------------------------------------ |
| **IntelliJ IDEA / VS Code**    | Java source code development         | `.java` files in `src/`                                |
| **Spring Boot 4.0.6**          | Web framework (MVC, JDBC, Thymeleaf) | `pom.xml` dependencies                                 |
| **SQLite**                     | Embedded database (no server needed) | `booking.db`, `schema.sql`, `data.sql`                 |
| **Maven**                      | Build tool, dependency management    | `pom.xml`, `.mvn/wrapper/`                             |
| **Git**                        | Version control                      | Git log below                                          |
| **Bootstrap 5**                | UI framework                         | Thymeleaf templates in `src/main/resources/templates/` |
| **Bootstrap Icons**            | Icon set for UI                      | Linked via CDN in templates                            |
| **draw.io (app.diagrams.net)** | UML/DFD diagram creation             | `docs/diagrams/*.drawio`                               |
| **python-docx**                | Generate Word requirements document  | `docs/generate_docx.py`                                |
| **JUnit 5 + Spring Boot Test** | Automated unit & integration testing | `src/test/`                                            |
| **Docker**                     | Containerization for deployment      | `Dockerfile`                                           |
| **BCrypt**                     | Password hashing (security)          | `AuthService.java`                                     |

---

## 3. Git Commit History

```
b4710e6  leekunmmm  2026-04-28  Dự án khách sạn luxehaven
```

---

## 4. Source Code Structure

```
src/
├── main/
│   ├── java/com/luxehaven/booking/
│   │   ├── BookingApplication.java          # Entry point
│   │   ├── config/                          # WebConfig, interceptors
│   │   ├── controller/
│   │   │   ├── AdminController.java         # Admin dashboard, CRUD
│   │   │   ├── AuthController.java          # Login, register
│   │   │   ├── BookingController.java       # Booking flow
│   │   │   ├── HomeController.java          # Home, room list, search
│   │   │   └── GlobalModelAttributes.java  # Shared model data
│   │   ├── dto/                             # Data Transfer Objects
│   │   ├── exception/                       # Custom exceptions
│   │   ├── model/                           # Entity classes + enums
│   │   │   ├── User.java
│   │   │   ├── Room.java
│   │   │   ├── Booking.java
│   │   │   ├── Voucher.java
│   │   │   └── (enums: Role, RoomType, RoomStatus, ...)
│   │   ├── repository/                      # JDBC data access layer
│   │   └── service/
│   │       ├── AuthService.java
│   │       ├── BookingService.java
│   │       ├── RoomService.java
│   │       ├── UserService.java
│   │       └── VoucherService.java
│   └── resources/
│       ├── templates/                       # Thymeleaf HTML templates
│       ├── static/                          # CSS, JS, images
│       ├── schema.sql                       # DB schema
│       ├── data.sql                         # Seed data
│       └── application.properties
└── test/
    └── java/com/luxehaven/booking/
        ├── BookingApplicationTests.java
        ├── service/AuthServiceTest.java
        └── service/BookingServiceTest.java
```

---

## 5. Testing Evidence

Automated tests were run using Maven:

```bash
mvn test
```

Test classes:

- `BookingApplicationTests.java` — Spring context load test
- `AuthServiceTest.java` — Unit tests for authentication service
- `BookingServiceTest.java` — Unit tests for booking service logic

Manual test cases are documented in `docs/test-cases.md` (30 test cases: TC-01 to TC-30).

---

## 6. How to Run the Application

### Option A — Maven (local)

```bash
mvn spring-boot:run
# Access: http://localhost:8080
```

### Option B — Docker

```bash
docker build -t luxehaven .
docker run -p 8080:8080 -v $(pwd)/uploads:/app/uploads luxehaven
# Access: http://localhost:8080
```

### Default accounts (from data.sql)

| Username  | Password | Role     |
| --------- | -------- | -------- |
| admin     | admin123 | ADMIN    |
| customer1 | 123456   | CUSTOMER |
