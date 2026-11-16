# Hệ Thống Quản Lý Đặt Vé Du Lịch (Web-Du-Lich)
## Travel Ticket Booking & Management Portal (2 Nhóm Phương Tiện: Vé Máy Bay & Vé Tàu Hỏa)

Ứng dụng web quản lý đặt vé du lịch và đăng ký lịch hẹn tư vấn phục vụ **2 nhóm phương tiện vận tải trọng yếu**: **1. Vé máy bay (Air), 2. Vé tàu hỏa (Train)** theo đúng Rubric IV (2/4 Nhóm Phương tiện) trong đề cương đồ án tốt nghiệp của Giảng viên.
Xây dựng trên nền tảng **Java 17, Spring Boot 3.2.5, Spring Web MVC, Spring Security 6, Spring Data JPA, Thymeleaf và Microsoft SQL Server**.

> **Kiến trúc CSDL tinh gọn chuẩn 3NF gồm đúng 5 BẢNG CỐT LÕI**, tuân thủ 100% tài liệu và rubric chấm điểm của Giảng viên.

---

## 1. Thiết Kế Cơ Sở Dữ Liệu 5 Bảng (SQL Server)

Hệ thống quy tụ toàn bộ dữ liệu vào **đúng 5 bảng có liên kết chặt chẽ**, loại bỏ tình trạng phân mảnh bảng:

| STT | Bảng | Khóa chính / Khóa ngoại | Chức năng chính trong hệ thống |
| :---: | :--- | :--- | :--- |
| **1** | **`users`** | `PK: id` | Tài khoản & Phân quyền RBAC 4 vai trò: `ROLE_TRAVELER`, `ROLE_AGENT`, `ROLE_OPERATOR`, `ROLE_ADMIN`. |
| **2** | **`appointments`**| `PK: id`<br>`FK: user_id -> users`<br>`FK: assigned_operator_id -> users` | Tiếp nhận đăng ký lịch hẹn tư vấn trực tiếp (Workflow 4), gán nhân viên phụ trách. |
| **3** | **`reservations`**| `PK: id`<br>`FK: user_id -> users` | Đơn đặt giữ chỗ (Workflow 1). Vòng đời: `CREATED` (giữ 15p) &rarr; `CONFIRMED` &rarr; `CANCELLED`. |
| **4** | **`tickets`** | `PK: id`<br>`FK: reservation_id -> reservations` | Kho chỗ ngồi khả dụng & chi tiết vé của từng hành khách, lưu số ghế, giá vé, mã vé điện tử và dữ liệu ảnh QR Code. |
| **5** | **`trans_details`**| `PK: id`<br>`FK: reservation_id -> reservations` | Lịch sử giao dịch thanh toán (Workflow 3): VNPAY-QR, Thẻ, Chuyển khoản, lưu vết đối soát. |

---

## 2. Kiến Trúc Hướng Đối Tượng & Mẫu Thiết Kế (Design Patterns)

1. **Mô hình 4 tầng chuẩn Enterprise (4-Tier Architecture):**
   - **Tầng 1 (Presentation Tier):** Thymeleaf SSR + Responsive HTML5/CSS3.
   - **Tầng 2 (Controller Tier):** `DispatcherServlet` & các Controller: `TicketCatalogController`, `ReservationController`, `PaymentController`, `AppointmentController`.
   - **Tầng 3 (Business Logic Tier):** Quản lý giao dịch `@Transactional(rollbackFor = Exception.class)`, State Machine, và **Strategy Pattern** cho 2 loại phương tiện.
   - **Tầng 4 (Persistence Tier):** HikariCP Connection Pool, Spring Data JPA Repositories và Microsoft SQL Server (ACID).

2. **Strategy Pattern (Vé Máy Bay & Tàu Hỏa) - Tuân thủ nguyên lý Open/Closed (OCP):**
   - Interface: `TicketStrategy` + `TicketStrategyFactory`
   - Implementation 1: `AirTicketStrategy` (Máy bay: Thuế phí sân bay 120k, tỉ lệ trẻ em/người lớn, thẩm định CCCD/hộ chiếu, hủy vé theo mốc 24h).
   - Implementation 2: `TrainTicketStrategy` (Tàu hỏa: Bảo hiểm đường sắt 10k, ưu đãi sinh viên -10%, người cao tuổi -15%, trẻ em -25%, chọn ghế toa).

3. **Mô hình POST / Redirect / GET (PRG Pattern):**
   - Chặn đứng hoàn toàn nguy cơ bấm Refresh (F5) làm gửi lặp đơn đặt vé hoặc trừ tiền 2 lần.

4. **Vé Điện Tử & Mã QR Code (ZXing Offline Engine):**
   - Tự động sinh mã vé điện tử chuẩn (`ETK-AIR-...`, `ETK-TRN-...`).
   - Tự động kết xuất ảnh mã QR Code dạng Base64 hiển thị trực tiếp trên thẻ vé lên phương tiện.

---

## 3. Tài Khoản Đăng Nhập Mẫu (RBAC 4 Vai Trò)

| Tài khoản | Mật khẩu | Họ tên | Vai trò (Spring Security) | Quyền hạn chính |
| :--- | :--- | :--- | :--- | :--- |
| **`admin`** | `admin123` | Quản Trị Viên Hệ Thống | `ROLE_ADMIN` | Toàn quyền cấu hình, phân quyền người dùng, báo cáo doanh thu. |
| **`operator_mai`** | `operator123` | Nguyễn Thị Mai | `ROLE_OPERATOR` | Điều phối lịch hẹn tư vấn, quản lý kho vé, kiểm soát đơn đặt chỗ. |
| **`agent_saigontour`** | `agent123` | Đại Lý Sài Gòn | `ROLE_AGENT` | Đặt vé đại lý, giữ chỗ theo nhóm số lượng lớn. |
| **`traveler_an`** | `traveler123` | Trần Văn An | `ROLE_TRAVELER` | Đặt vé cá nhân, xem và in vé điện tử, tra cứu đơn của mình. |

---

## 4. Hướng Dẫn Chạy Project & Nạp CSDL

### Bước 1: Tạo Database trong SQL Server
Mở **SQL Server Management Studio (SSMS)** và chạy file:
- `src/main/resources/sql/schema.sql` (Tạo đúng 5 bảng và các ràng buộc FK/Index).
- `src/main/resources/sql/data.sql` (Nạp dữ liệu mẫu 2 phương tiện và tài khoản).

### Bước 2: Thiết lập mật khẩu SQL Server
- Trên **PowerShell**:
  ```powershell
  $env:DB_PASSWORD = "mat_khau_sql_cua_ban"
  ```
- Hoặc trên **CMD**:
  ```cmd
  set DB_PASSWORD=mat_khau_sql_cua_ban
  ```

### Bước 3: Chạy ứng dụng Spring Boot
```powershell
.\mvnw.cmd spring-boot:run
```

### Bước 4: Truy cập ứng dụng
- Trang chủ người dùng: [http://localhost:8080](http://localhost:8080)
- Tra cứu danh mục vé Máy Bay & Tàu Hỏa: [http://localhost:8080/catalog](http://localhost:8080/catalog)
- Đăng ký lịch hẹn tư vấn: [http://localhost:8080/appointments/schedule](http://localhost:8080/appointments/schedule)
- Trang quản trị điều hành (Admin & Operator): [http://localhost:8080/admin](http://localhost:8080/admin)
