-- ==========================================================================================
-- HỆ THỐNG QUẢN LÝ ĐẶT VÉ DU LỊCH (TRAVEL TICKET BOOKING & MANAGEMENT PORTAL)
-- CSDL: Microsoft SQL Server (DatVeDuLich)
-- CẤU TRÚC: CHUẨN 5 BẢNG CỐT LÕI (Users, Appointments, Reservations, Tickets, TransDetails)
-- Phạm vi: 2 Nhóm Phương tiện chuyên biệt (VÉ MÁY BAY & VÉ TÀU HỎA) theo Rubric 2/4 của Giảng viên
-- ==========================================================================================

USE DatVeDuLich;
GO

-- 1. XÓA BẢNG NẾU ĐÃ TỒN TẠI (Theo đúng thứ tự ràng buộc khóa ngoại FK)
IF OBJECT_ID('dbo.trans_details', 'U') IS NOT NULL DROP TABLE dbo.trans_details;
IF OBJECT_ID('dbo.payments', 'U') IS NOT NULL DROP TABLE dbo.payments;
IF OBJECT_ID('dbo.tickets', 'U') IS NOT NULL DROP TABLE dbo.tickets;
IF OBJECT_ID('dbo.ticket_details', 'U') IS NOT NULL DROP TABLE dbo.ticket_details;
IF OBJECT_ID('dbo.flight_passengers', 'U') IS NOT NULL DROP TABLE dbo.flight_passengers;
IF OBJECT_ID('dbo.train_tickets', 'U') IS NOT NULL DROP TABLE dbo.train_tickets;
IF OBJECT_ID('dbo.flight_bookings', 'U') IS NOT NULL DROP TABLE dbo.flight_bookings;
IF OBJECT_ID('dbo.train_bookings', 'U') IS NOT NULL DROP TABLE dbo.train_bookings;
IF OBJECT_ID('dbo.train_seats', 'U') IS NOT NULL DROP TABLE dbo.train_seats;
IF OBJECT_ID('dbo.train_coaches', 'U') IS NOT NULL DROP TABLE dbo.train_coaches;
IF OBJECT_ID('dbo.train_stations', 'U') IS NOT NULL DROP TABLE dbo.train_stations;
IF OBJECT_ID('dbo.trains', 'U') IS NOT NULL DROP TABLE dbo.trains;
IF OBJECT_ID('dbo.flights', 'U') IS NOT NULL DROP TABLE dbo.flights;
IF OBJECT_ID('dbo.reservations', 'U') IS NOT NULL DROP TABLE dbo.reservations;
IF OBJECT_ID('dbo.bookings', 'U') IS NOT NULL DROP TABLE dbo.bookings;
IF OBJECT_ID('dbo.appointments', 'U') IS NOT NULL DROP TABLE dbo.appointments;
IF OBJECT_ID('dbo.consultations', 'U') IS NOT NULL DROP TABLE dbo.consultations;
IF OBJECT_ID('dbo.customers', 'U') IS NOT NULL DROP TABLE dbo.customers;
IF OBJECT_ID('dbo.users', 'U') IS NOT NULL DROP TABLE dbo.users;
GO

-- ------------------------------------------------------------------------------------------
-- BẢNG 1: users (Tài khoản người dùng, phân quyền 4 vai trò RBAC)
-- ------------------------------------------------------------------------------------------
CREATE TABLE dbo.users (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name NVARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20) NULL,
    role VARCHAR(30) NOT NULL CONSTRAINT DF_users_role DEFAULT 'ROLE_TRAVELER',
    status BIT NOT NULL CONSTRAINT DF_users_status DEFAULT 1,
    created_at DATETIME2 NOT NULL CONSTRAINT DF_users_created_at DEFAULT GETDATE(),

    CONSTRAINT CK_users_role CHECK (role IN ('ROLE_TRAVELER', 'ROLE_AGENT', 'ROLE_OPERATOR', 'ROLE_ADMIN'))
);
GO

-- ------------------------------------------------------------------------------------------
-- BẢNG 2: appointments (Đăng ký lịch hẹn tư vấn chuyến đi / vé đoàn trực tiếp)
-- ------------------------------------------------------------------------------------------
CREATE TABLE dbo.appointments (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    appointment_code VARCHAR(30) NOT NULL UNIQUE,
    user_id BIGINT NULL,                             -- Khách hàng hoặc Đại lý đặt lịch (nếu có tài khoản)
    customer_name NVARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(100) NULL,
    preferred_date DATETIME2 NOT NULL,               -- Thời gian hẹn tư vấn mong muốn
    transport_type VARCHAR(20) NOT NULL CONSTRAINT DF_appointments_transport DEFAULT 'AIR',
    notes NVARCHAR(500) NULL,
    status VARCHAR(20) NOT NULL CONSTRAINT DF_appointments_status DEFAULT 'PENDING',
    assigned_operator_id BIGINT NULL,                -- Nhân viên điều hành / tư vấn viên phụ trách
    created_at DATETIME2 NOT NULL CONSTRAINT DF_appointments_created_at DEFAULT GETDATE(),

    CONSTRAINT FK_appointments_user FOREIGN KEY (user_id) REFERENCES dbo.users(id),
    CONSTRAINT FK_appointments_operator FOREIGN KEY (assigned_operator_id) REFERENCES dbo.users(id),
    CONSTRAINT CK_appointments_transport CHECK (transport_type IN ('AIR', 'TRAIN')),
    CONSTRAINT CK_appointments_status CHECK (status IN ('PENDING', 'CONFIRMED', 'COMPLETED', 'CANCELLED'))
);
GO

-- ------------------------------------------------------------------------------------------
-- BẢNG 3: reservations (Đơn đặt giữ chỗ cho Vé Máy Bay & Vé Tàu Hỏa)
-- ------------------------------------------------------------------------------------------
CREATE TABLE dbo.reservations (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    booking_code VARCHAR(30) NOT NULL UNIQUE,
    user_id BIGINT NULL,                             -- Tài khoản hội viên/đại lý (nullable với khách vãng lai)
    customer_name NVARCHAR(100) NOT NULL,
    customer_phone VARCHAR(20) NOT NULL,
    customer_email VARCHAR(100) NOT NULL,
    transport_type VARCHAR(20) NOT NULL,             -- AIR, TRAIN
    departure_place NVARCHAR(100) NOT NULL,
    arrival_place NVARCHAR(100) NOT NULL,
    departure_date DATETIME2 NOT NULL,
    return_date DATETIME2 NULL,
    is_round_trip BIT NOT NULL CONSTRAINT DF_reservations_round_trip DEFAULT 0,
    passenger_count INT NOT NULL CONSTRAINT DF_reservations_passenger_count DEFAULT 1,
    total_amount DECIMAL(18,2) NOT NULL CONSTRAINT DF_reservations_total_amount DEFAULT 0,
    status VARCHAR(20) NOT NULL CONSTRAINT DF_reservations_status DEFAULT 'CREATED',
    hold_expires_at DATETIME2 NOT NULL,              -- Hạn chót giữ chỗ (15 phút)
    cancel_reason NVARCHAR(255) NULL,
    created_at DATETIME2 NOT NULL CONSTRAINT DF_reservations_created_at DEFAULT GETDATE(),
    updated_at DATETIME2 NULL,

    CONSTRAINT FK_reservations_user FOREIGN KEY (user_id) REFERENCES dbo.users(id),
    CONSTRAINT CK_reservations_transport CHECK (transport_type IN ('AIR', 'TRAIN')),
    CONSTRAINT CK_reservations_status CHECK (status IN ('CREATED', 'CONFIRMED', 'CANCELLED', 'EXPIRED')),
    CONSTRAINT CK_reservations_passenger_count CHECK (passenger_count >= 1)
);
GO

-- ------------------------------------------------------------------------------------------
-- BẢNG 4: tickets (Kho vé khả dụng & Chi tiết vé/ghế đã đặt của từng hành khách)
-- ------------------------------------------------------------------------------------------
CREATE TABLE dbo.tickets (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    reservation_id BIGINT NULL,                      -- NULL: Vé trong kho khả dụng; NOT NULL: Đã được gán vào đơn đặt
    transport_type VARCHAR(20) NOT NULL,             -- AIR, TRAIN
    vehicle_code VARCHAR(50) NOT NULL,               -- Số hiệu chuyến bay (VN210, VJ134) hoặc Số hiệu tàu (SE1, SE3)
    vehicle_name NVARCHAR(100) NOT NULL,             -- Hãng hàng không / Đoàn tàu Thống Nhất
    route_info NVARCHAR(200) NOT NULL,               -- Tuyến đường: Hà Nội -> Sài Gòn, Hà Nội -> Đà Nẵng...
    departure_place NVARCHAR(100) NOT NULL,
    arrival_place NVARCHAR(100) NOT NULL,
    departure_time DATETIME2 NOT NULL,
    arrival_time DATETIME2 NOT NULL,
    seat_number NVARCHAR(50) NOT NULL,               -- Số ghế máy bay (12A) hoặc Toa/Ghế tàu (Toa 4 - Giường 11)
    seat_class VARCHAR(50) NOT NULL,                 -- ECONOMY, BUSINESS hoặc SOFT_SEAT, SLEEPER_4
    passenger_name NVARCHAR(100) NULL,               -- Họ tên hành khách (sau khi đặt)
    id_card VARCHAR(30) NULL,                        -- Số CCCD hoặc Hộ chiếu
    passenger_type VARCHAR(20) NOT NULL CONSTRAINT DF_tickets_passenger_type DEFAULT 'ADULT',
    base_price DECIMAL(18,2) NOT NULL DEFAULT 0,
    service_fee DECIMAL(18,2) NOT NULL DEFAULT 0,     -- Thuế sân bay, bảo hiểm đường sắt...
    extra_fee DECIMAL(18,2) NOT NULL DEFAULT 0,       -- Phí hành lý quá cước, suất ăn...
    final_price DECIMAL(18,2) NOT NULL DEFAULT 0,
    ticket_code VARCHAR(50) NULL,                    -- Mã vé điện tử: ETK-AIR-..., ETK-TRN-...
    qr_code_data NVARCHAR(MAX) NULL,                 -- Dữ liệu chuỗi / Base64 / URL phục vụ quét mã QR Code
    status VARCHAR(20) NOT NULL CONSTRAINT DF_tickets_status DEFAULT 'AVAILABLE',
    created_at DATETIME2 NOT NULL CONSTRAINT DF_tickets_created_at DEFAULT GETDATE(),

    CONSTRAINT FK_tickets_reservation FOREIGN KEY (reservation_id) REFERENCES dbo.reservations(id) ON DELETE SET NULL,
    CONSTRAINT CK_tickets_transport CHECK (transport_type IN ('AIR', 'TRAIN')),
    CONSTRAINT CK_tickets_passenger_type CHECK (passenger_type IN ('ADULT', 'CHILD', 'INFANT', 'SENIOR', 'STUDENT')),
    CONSTRAINT CK_tickets_status CHECK (status IN ('AVAILABLE', 'HELD', 'ISSUED', 'CANCELLED'))
);
GO

-- ------------------------------------------------------------------------------------------
-- BẢNG 5: trans_details (Lịch sử giao dịch thanh toán - Tuân thủ chuẩn tài liệu của Thầy)
-- ------------------------------------------------------------------------------------------
CREATE TABLE dbo.trans_details (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    reservation_id BIGINT NOT NULL,                  -- Đơn đặt vé cần thanh toán
    payment_code VARCHAR(50) NOT NULL UNIQUE,
    amount DECIMAL(18,2) NOT NULL,
    payment_method VARCHAR(30) NOT NULL,             -- VNPAY, BANK_TRANSFER, CREDIT_CARD, CASH
    payment_status VARCHAR(20) NOT NULL CONSTRAINT DF_trans_details_status DEFAULT 'SUCCESS',
    transaction_reference VARCHAR(100) NULL,         -- Mã tham chiếu ngân hàng / cổng thanh toán
    note NVARCHAR(255) NULL,
    transaction_time DATETIME2 NOT NULL CONSTRAINT DF_trans_details_time DEFAULT GETDATE(),

    CONSTRAINT FK_trans_details_reservation FOREIGN KEY (reservation_id) REFERENCES dbo.reservations(id) ON DELETE CASCADE,
    CONSTRAINT CK_trans_details_method CHECK (payment_method IN ('VNPAY', 'BANK_TRANSFER', 'CREDIT_CARD', 'CASH')),
    CONSTRAINT CK_trans_details_status CHECK (payment_status IN ('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED'))
);
GO

-- ------------------------------------------------------------------------------------------
-- TẠO CHỈ MỤC (INDEXES) TỐI ƯU HIỆU NĂNG CHO HIKARICP VÀ TRUY VẤN ĐỒNG THỜI
-- ------------------------------------------------------------------------------------------
CREATE INDEX IX_appointments_user_id ON dbo.appointments(user_id);
CREATE INDEX IX_appointments_operator_id ON dbo.appointments(assigned_operator_id);
CREATE INDEX IX_appointments_phone ON dbo.appointments(phone);

CREATE INDEX IX_reservations_booking_code ON dbo.reservations(booking_code);
CREATE INDEX IX_reservations_user_id ON dbo.reservations(user_id);
CREATE INDEX IX_reservations_customer_phone ON dbo.reservations(customer_phone);
CREATE INDEX IX_reservations_status ON dbo.reservations(status);

CREATE INDEX IX_tickets_reservation_id ON dbo.tickets(reservation_id);
CREATE INDEX IX_tickets_search ON dbo.tickets(transport_type, departure_place, arrival_place, departure_time, status);
CREATE INDEX IX_tickets_code ON dbo.tickets(ticket_code);

CREATE INDEX IX_trans_details_reservation_id ON dbo.trans_details(reservation_id);
CREATE INDEX IX_trans_details_code ON dbo.trans_details(payment_code);
GO
