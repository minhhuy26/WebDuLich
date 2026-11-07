-- ==========================================================================================
-- HỆ THỐNG QUẢN LÝ ĐẶT VÉ DU LỊCH (VÉ MÁY BAY & VÉ TÀU HỎA)
-- CSDL: SQL Server (DatVeDuLich)
-- Tác vụ: Tạo cấu trúc các bảng dùng chung và các ràng buộc dữ liệu
-- ==========================================================================================

USE DatVeDuLich;
GO

-- 1. XÓA BẢNG NẾU ĐÃ TỒN TẠI (Thứ tự xóa theo quan hệ Khóa ngoại FK)
IF OBJECT_ID('dbo.ticket_details', 'U') IS NOT NULL DROP TABLE dbo.ticket_details;
IF OBJECT_ID('dbo.payments', 'U') IS NOT NULL DROP TABLE dbo.payments;
IF OBJECT_ID('dbo.consultations', 'U') IS NOT NULL DROP TABLE dbo.consultations;
IF OBJECT_ID('dbo.bookings', 'U') IS NOT NULL DROP TABLE dbo.bookings;
IF OBJECT_ID('dbo.customers', 'U') IS NOT NULL DROP TABLE dbo.customers;
IF OBJECT_ID('dbo.users', 'U') IS NOT NULL DROP TABLE dbo.users;
GO

-- ------------------------------------------------------------------------------------------
-- BẢNG 1: users (Người dùng nội bộ: Quản trị viên Admin & Nhân viên tư vấn/bán vé Staff)
-- ------------------------------------------------------------------------------------------
CREATE TABLE dbo.users (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name NVARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20) NULL,
    role VARCHAR(20) NOT NULL CONSTRAINT DF_users_role DEFAULT 'STAFF',
    status BIT NOT NULL CONSTRAINT DF_users_status DEFAULT 1,
    created_at DATETIME2 NOT NULL CONSTRAINT DF_users_created_at DEFAULT GETDATE(),

    CONSTRAINT CK_users_role CHECK (role IN ('ADMIN', 'NHANVIEN', 'KHACH', 'STAFF'))
);
GO

-- ------------------------------------------------------------------------------------------
-- BẢNG 2: customers (Khách hàng đặt vé / cần tư vấn)
-- ------------------------------------------------------------------------------------------
CREATE TABLE dbo.customers (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    full_name NVARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(100) NOT NULL,
    id_card VARCHAR(20) NULL, -- CCCD / Hộ chiếu
    address NVARCHAR(255) NULL,
    created_at DATETIME2 NOT NULL CONSTRAINT DF_customers_created_at DEFAULT GETDATE()
);
GO

-- ------------------------------------------------------------------------------------------
-- BẢNG 3: bookings (Đơn đặt vé dùng chung cho cả Vé máy bay và Vé tàu hỏa)
-- ------------------------------------------------------------------------------------------
CREATE TABLE dbo.bookings (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    booking_code VARCHAR(30) NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL,
    ticket_type VARCHAR(20) NOT NULL, -- 'FLIGHT' hoặc 'TRAIN'
    booking_date DATETIME2 NOT NULL CONSTRAINT DF_bookings_booking_date DEFAULT GETDATE(),
    departure_date DATETIME2 NOT NULL,
    return_date DATETIME2 NULL,
    departure_place NVARCHAR(100) NOT NULL, -- Sân bay đi / Ga đi
    arrival_place NVARCHAR(100) NOT NULL,   -- Sân bay đến / Ga đến
    passenger_count INT NOT NULL CONSTRAINT DF_bookings_passenger_count DEFAULT 1,
    total_amount DECIMAL(18,2) NOT NULL CONSTRAINT DF_bookings_total_amount DEFAULT 0,
    status VARCHAR(20) NOT NULL CONSTRAINT DF_bookings_status DEFAULT 'PENDING',
    special_requests NVARCHAR(500) NULL,
    created_at DATETIME2 NOT NULL CONSTRAINT DF_bookings_created_at DEFAULT GETDATE(),
    updated_at DATETIME2 NULL,

    CONSTRAINT FK_bookings_customer FOREIGN KEY (customer_id) REFERENCES dbo.customers(id),
    CONSTRAINT CK_bookings_ticket_type CHECK (ticket_type IN ('FLIGHT', 'TRAIN')),
    CONSTRAINT CK_bookings_status CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED')),
    CONSTRAINT CK_bookings_passenger_count CHECK (passenger_count >= 1)
);
GO

-- ------------------------------------------------------------------------------------------
-- BẢNG 4: ticket_details (Chi tiết hành khách, số hiệu chuyến, hạng ghế, giá vé thành phần)
-- ------------------------------------------------------------------------------------------
CREATE TABLE dbo.ticket_details (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    passenger_name NVARCHAR(100) NOT NULL,
    passenger_id_card VARCHAR(20) NULL,
    passenger_type VARCHAR(20) NOT NULL CONSTRAINT DF_ticket_details_passenger_type DEFAULT 'ADULT',
    vehicle_code VARCHAR(50) NOT NULL, -- Số hiệu chuyến bay (VD: VN210) hoặc Số hiệu tàu (VD: SE1)
    seat_class VARCHAR(50) NOT NULL,   -- Hạng vé (ECONOMY/BUSINESS hoặc SOFT_SEAT/SLEEPER_4/SLEEPER_6)
    seat_number VARCHAR(20) NULL,      -- Số ghế máy bay hoặc Toa/Số giường tàu
    base_price DECIMAL(18,2) NOT NULL DEFAULT 0,
    service_fee DECIMAL(18,2) NOT NULL DEFAULT 0,
    extra_fee DECIMAL(18,2) NOT NULL DEFAULT 0, -- Phí hành lý ký gửi hoặc bảo hiểm/suất ăn
    final_price DECIMAL(18,2) NOT NULL DEFAULT 0,
    created_at DATETIME2 NOT NULL CONSTRAINT DF_ticket_details_created_at DEFAULT GETDATE(),

    CONSTRAINT FK_ticket_details_booking FOREIGN KEY (booking_id) REFERENCES dbo.bookings(id) ON DELETE CASCADE,
    CONSTRAINT CK_ticket_details_passenger_type CHECK (passenger_type IN ('ADULT', 'CHILD', 'INFANT'))
);
GO

-- ------------------------------------------------------------------------------------------
-- BẢNG 5: payments (Lịch sử thanh toán giao dịch đặt vé)
-- ------------------------------------------------------------------------------------------
CREATE TABLE dbo.payments (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    payment_code VARCHAR(30) NOT NULL UNIQUE,
    booking_id BIGINT NOT NULL,
    amount DECIMAL(18,2) NOT NULL,
    payment_method VARCHAR(30) NOT NULL, -- CASH, BANK_TRANSFER, CREDIT_CARD, VN_PAY
    payment_date DATETIME2 NOT NULL CONSTRAINT DF_payments_payment_date DEFAULT GETDATE(),
    payment_status VARCHAR(20) NOT NULL CONSTRAINT DF_payments_status DEFAULT 'PENDING',
    transaction_reference VARCHAR(100) NULL,
    note NVARCHAR(255) NULL,
    created_at DATETIME2 NOT NULL CONSTRAINT DF_payments_created_at DEFAULT GETDATE(),

    CONSTRAINT FK_payments_booking FOREIGN KEY (booking_id) REFERENCES dbo.bookings(id) ON DELETE CASCADE,
    CONSTRAINT CK_payments_method CHECK (payment_method IN ('CASH', 'BANK_TRANSFER', 'CREDIT_CARD', 'VN_PAY')),
    CONSTRAINT CK_payments_status CHECK (payment_status IN ('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED'))
);
GO

-- ------------------------------------------------------------------------------------------
-- BẢNG 6: consultations (Đăng ký lịch hẹn tư vấn chuyến đi / đặt vé)
-- ------------------------------------------------------------------------------------------
CREATE TABLE dbo.consultations (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    consultation_code VARCHAR(30) NOT NULL UNIQUE,
    customer_name NVARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(100) NULL,
    ticket_type VARCHAR(20) NOT NULL, -- FLIGHT hoặc TRAIN
    preferred_date DATETIME2 NOT NULL,
    status VARCHAR(20) NOT NULL CONSTRAINT DF_consultations_status DEFAULT 'PENDING',
    notes NVARCHAR(500) NULL,
    assigned_user_id BIGINT NULL,
    created_at DATETIME2 NOT NULL CONSTRAINT DF_consultations_created_at DEFAULT GETDATE(),

    CONSTRAINT FK_consultations_user FOREIGN KEY (assigned_user_id) REFERENCES dbo.users(id),
    CONSTRAINT CK_consultations_ticket_type CHECK (ticket_type IN ('FLIGHT', 'TRAIN')),
    CONSTRAINT CK_consultations_status CHECK (status IN ('PENDING', 'CONFIRMED', 'COMPLETED', 'CANCELLED'))
);
GO

-- TẠO CHỈ MỤC (INDEXES) TỐI ƯU TRUY VẤN
CREATE INDEX IX_bookings_customer_id ON dbo.bookings(customer_id);
CREATE INDEX IX_bookings_ticket_type ON dbo.bookings(ticket_type);
CREATE INDEX IX_bookings_status ON dbo.bookings(status);
CREATE INDEX IX_ticket_details_booking_id ON dbo.ticket_details(booking_id);
CREATE INDEX IX_payments_booking_id ON dbo.payments(booking_id);
CREATE INDEX IX_consultations_phone ON dbo.consultations(phone);
GO
