# Hệ Thống Quản Lý Đặt Vé Du Lịch (Web-Du-Lich)

Ứng dụng web quản lý đặt vé du lịch phục vụ 2 loại vé chuyên biệt: **VÉ MÁY BAY** và **VÉ TÀU HỎA**, được xây dựng trên nền tảng **Java 17, Spring Boot, Spring Web MVC, Spring Data JPA, Thymeleaf và Microsoft SQL Server**.

---

## 1. Cấu trúc Project
```text
Web-Du-Lich/
├── .mvn/wrapper/                 # Maven wrapper
├── mvnw / mvnw.cmd               # Maven wrapper script
├── pom.xml                       # Quản lý dependencies (Spring Boot 3.2.5, JPA, Thymeleaf, SQL Server JDBC)
└── src/
    ├── main/
    │   ├── java/com/dulich/ticketmanagement/
    │   │   ├── config/           # Cấu hình DataSource, Runner kiểm tra kết nối DB lúc khởi động
    │   │   ├── controller/       # Spring MVC Controllers (HomeController)
    │   │   ├── dto/              # Data Transfer Objects (SystemStatsDto, ConsultationRequestDto)
    │   │   ├── entity/           # JPA Entities (User, Customer, Booking, TicketDetail, Payment, Consultation)
    │   │   │   └── enums/        # TicketType, BookingStatus, PaymentMethod, PaymentStatus, UserRole...
    │   │   ├── exception/        # Xử lý lỗi tập trung @ControllerAdvice, BusinessException, ResourceNotFoundException
    │   │   ├── repository/       # Spring Data JPA Repositories (UserRepository, BookingRepository, ...)
    │   │   ├── service/          # TicketService (interface chung), TicketServiceFactory, BookingService, ConsultationService
    │   │   │   └── impl/         # FlightTicketService, TrainTicketService, BookingServiceImpl, ConsultationServiceImpl
    │   │   └── WebDuLichApplication.java # Lớp khởi chạy Spring Boot chính
    │   └── resources/
    │       ├── application.properties    # Cấu hình DB, port, JPA, Thymeleaf
    │       ├── sql/
    │       │   ├── schema.sql    # File SQL DDL tạo bảng, ràng buộc FK/CK/Indexes
    │       │   └── data.sql      # File SQL DML chèn dữ liệu mẫu ban đầu
    │       ├── static/           # Static assets (css, js, images)
    │       └── templates/        # Thymeleaf views (home.html, error.html)
```

---

## 2. Thiết Kế Bảng và Lý Do Thiết Kế

| Bảng | Chức năng chính | Lý do thiết kế |
| :--- | :--- | :--- |
| **`users`** | Tài khoản quản trị (ADMIN) & nhân viên tư vấn/điều hành vé (STAFF). | Phục vụ phân quyền và quản lý ai phụ trách tư vấn lịch hẹn hoặc ai xử lý đơn vé. |
| **`customers`** | Hồ sơ khách hàng (Họ tên, SĐT, Email, CCCD/Hộ chiếu, Địa chỉ). | Chuẩn hóa dữ liệu (3NF), 1 khách hàng có thể đặt nhiều đơn vé hoặc đăng ký nhiều lần tư vấn mà không bị trùng lặp dữ liệu. |
| **`bookings`** | Đơn đặt giữ chỗ / đặt vé (Mã đơn, loại vé: FLIGHT/TRAIN, ngày đi/về, ga/sân bay đi/đến, tổng tiền, trạng thái). | Header chung cho giao dịch đặt vé, quản lý vòng đời đơn (`PENDING`, `CONFIRMED`, `CANCELLED`, `COMPLETED`). |
| **`ticket_details`** | Danh sách hành khách và chi tiết vé trong đơn (Tên, CCCD, Hạng vé, Số ghế/giường, Số hiệu bay/tàu, Giá gốc, Phí dịch vụ, Phụ thu). | Một đơn đặt vé có thể đi theo nhóm nhiều người; hỗ trợ linh hoạt cho cả máy bay (Economy/Business) và tàu hỏa (Ghế cứng, Ghế mềm, Giường nằm). |
| **`payments`** | Lịch sử thanh toán giao dịch (Tiền mặt, Chuyển khoản, Thẻ, VNPAY, Mã tham chiếu GD). | Tách biệt thanh toán với đơn đặt vé, hỗ trợ quản lý trạng thái thanh toán, truy soát giao dịch ngân hàng và hoàn tiền khi hủy vé. |
| **`consultations`**| Đăng ký lịch hẹn tư vấn chuyến đi (Khách hàng, SĐT, Loại vé quan tâm, Giờ hẹn, Ghi chú yêu cầu). | Đáp ứng chức năng số 1 "Đăng ký lịch hẹn tư vấn", giúp nhân viên chủ động liên hệ trước khi khách chốt đặt vé. |

---

## 3. Kiến Trúc Riêng Biệt Cho 2 Loại Vé

Cả 2 loại vé đều tuân theo Interface chung `TicketService`, với 2 Service triển khai độc lập:
1. **`FlightTicketService` (Vé Máy Bay):**
   - **Quy trình:** Giữ chỗ PNR hàng không & xuất vé điện tử.
   - **Tính phí:** Phụ thu hạng vé (Business x1.8, Economy x1.0) + Thuế phí an ninh sân bay cố định 150.000đ/khách + Phụ phí hành lý.
   - **Validation:** Bắt buộc số CCCD/Hộ chiếu với khách người lớn; phải đặt trước giờ khởi hành tối thiểu 2 tiếng.
   - **Chính sách hủy:** Trước 24h phí 10%, trong vòng 24h phí 30%, chuyến bay đã cất cánh không được hủy.
2. **`TrainTicketService` (Vé Tàu Hỏa):**
   - **Quy trình:** Chọn sơ đồ toa tàu, vị trí ghế ngồi hoặc giường nằm.
   - **Tính phí:** Hệ số phân loại chỗ ngành đường sắt (Ghế cứng x0.85, Ghế mềm x1.0, Giường nằm khoang 6 x1.3, Giường nằm khoang 4 VIP x1.5) + Phí bảo hiểm đường sắt 10.000đ/khách.
   - **Validation:** Ga đi và Ga đến không được trùng nhau; phải đặt trước giờ tàu chạy tối thiểu 30 phút.
   - **Chính sách hủy:** Phải hủy trước giờ xuất phát tối thiểu 4 tiếng, áp dụng phí hoàn trả 20%.

---

## 4. Hướng Dẫn Chạy Project & Kiểm Tra Database

### Bước 1: Thiết lập biến môi trường mật khẩu DB
Mật khẩu SQL Server được đọc từ biến môi trường `DB_PASSWORD`:
- Trên **PowerShell**:
  ```powershell
  $env:DB_PASSWORD = "mat_khau_sql_cua_ban"
  ```
- Hoặc trên **Command Prompt (CMD)**:
  ```cmd
  set DB_PASSWORD=mat_khau_sql_cua_ban
  ```

### Bước 2: Thiết lập JAVA_HOME (nếu chưa cấu hình)
```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
```

### Bước 3: Chạy ứng dụng
Dùng Maven Wrapper tích hợp sẵn:
```powershell
.\mvnw.cmd spring-boot:run
```

### Bước 4: Kiểm tra kết nối Database & Truy cập Web
1. Mở trình duyệt và truy cập: [http://localhost:8080](http://localhost:8080)
2. Trên thanh trạng thái sẽ hiển thị:
   `ĐÃ KẾT NỐI THÀNH CÔNG (DatVeDuLich @ localhost:1433)` kèm bảng thống kê các bản ghi hiện tại trong database.
3. Trong log console của Spring Boot, bạn sẽ thấy thông báo:
   ```text
   [SUCCESS] KẾT NỐI DATABASE SQL SERVER THÀNH CÔNG!
   Database Product : Microsoft SQL Server ...
   Database URL     : jdbc:sqlserver://localhost:1433;databaseName=DatVeDuLich...
   Current User     : sa
   ```
