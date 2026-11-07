-- ==========================================================================================
-- BỔ SUNG CẤU TRÚC VÀ DỮ LIỆU CHUYÊN BIỆT CHO PHÂN HỆ VÉ MÁY BAY (FLIGHT MODULE)
-- ==========================================================================================

USE DatVeDuLich;
GO

-- 1. TẠO BẢNG flights (Danh mục chuyến bay)
IF OBJECT_ID('dbo.flight_passengers', 'U') IS NOT NULL DROP TABLE dbo.flight_passengers;
IF OBJECT_ID('dbo.flight_bookings', 'U') IS NOT NULL DROP TABLE dbo.flight_bookings;
IF OBJECT_ID('dbo.flights', 'U') IS NOT NULL DROP TABLE dbo.flights;
GO

CREATE TABLE dbo.flights (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    flight_number VARCHAR(20) NOT NULL UNIQUE,
    airline NVARCHAR(100) NOT NULL,
    departure_airport VARCHAR(10) NOT NULL,
    departure_city NVARCHAR(100) NOT NULL,
    arrival_airport VARCHAR(10) NOT NULL,
    arrival_city NVARCHAR(100) NOT NULL,
    departure_time DATETIME2 NOT NULL,
    arrival_time DATETIME2 NOT NULL,
    base_price DECIMAL(18,2) NOT NULL,
    economy_seats_total INT NOT NULL DEFAULT 150,
    economy_seats_available INT NOT NULL DEFAULT 150,
    premium_seats_total INT NOT NULL DEFAULT 30,
    premium_seats_available INT NOT NULL DEFAULT 30,
    business_seats_total INT NOT NULL DEFAULT 16,
    business_seats_available INT NOT NULL DEFAULT 16,
    status VARCHAR(20) NOT NULL CONSTRAINT DF_flights_status DEFAULT 'ACTIVE',
    created_at DATETIME2 NOT NULL CONSTRAINT DF_flights_created_at DEFAULT GETDATE()
);
GO

-- 2. TẠO BẢNG flight_bookings (Đơn đặt chỗ / phát hành vé máy bay)
CREATE TABLE dbo.flight_bookings (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    booking_code VARCHAR(30) NOT NULL UNIQUE,
    flight_id BIGINT NOT NULL,
    return_flight_id BIGINT NULL,
    is_round_trip BIT NOT NULL CONSTRAINT DF_flight_bookings_round_trip DEFAULT 0,
    customer_name NVARCHAR(100) NOT NULL,
    customer_phone VARCHAR(20) NOT NULL,
    customer_email VARCHAR(100) NOT NULL,
    seat_class VARCHAR(20) NOT NULL, -- ECONOMY, PREMIUM, BUSINESS
    return_seat_class VARCHAR(20) NULL,
    passenger_count INT NOT NULL DEFAULT 1,
    total_fare DECIMAL(18,2) NOT NULL DEFAULT 0,
    total_tax_fee DECIMAL(18,2) NOT NULL DEFAULT 0,
    total_baggage_fee DECIMAL(18,2) NOT NULL DEFAULT 0,
    total_amount DECIMAL(18,2) NOT NULL DEFAULT 0,
    refund_amount DECIMAL(18,2) NOT NULL DEFAULT 0,
    cancellation_fee DECIMAL(18,2) NOT NULL DEFAULT 0,
    status VARCHAR(30) NOT NULL CONSTRAINT DF_flight_bookings_status DEFAULT 'HELD', -- HELD, CONFIRMED, ISSUED, CANCELLED, CHANGED, EXPIRED
    hold_expires_at DATETIME2 NOT NULL,
    booking_time DATETIME2 NOT NULL CONSTRAINT DF_flight_bookings_booking_time DEFAULT GETDATE(),
    confirmed_time DATETIME2 NULL,
    cancelled_time DATETIME2 NULL,
    cancel_reason NVARCHAR(255) NULL,

    CONSTRAINT FK_flight_bookings_flight FOREIGN KEY (flight_id) REFERENCES dbo.flights(id),
    CONSTRAINT FK_flight_bookings_return_flight FOREIGN KEY (return_flight_id) REFERENCES dbo.flights(id),
    CONSTRAINT CK_flight_bookings_seat_class CHECK (seat_class IN ('ECONOMY', 'PREMIUM', 'BUSINESS')),
    CONSTRAINT CK_flight_bookings_status CHECK (status IN ('HELD', 'CONFIRMED', 'ISSUED', 'CANCELLED', 'CHANGED', 'EXPIRED'))
);
GO

-- 3. TẠO BẢNG flight_passengers (Hành khách trong đơn đặt vé máy bay)
CREATE TABLE dbo.flight_passengers (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    flight_booking_id BIGINT NOT NULL,
    full_name NVARCHAR(100) NOT NULL,
    id_card VARCHAR(20) NULL, -- CCCD 12 số hoặc Hộ chiếu
    passenger_type VARCHAR(20) NOT NULL CONSTRAINT DF_flight_passengers_type DEFAULT 'ADULT', -- ADULT, CHILD, INFANT
    seat_number VARCHAR(10) NULL,
    return_seat_number VARCHAR(10) NULL,
    baggage_kg INT NOT NULL DEFAULT 0,
    base_fare DECIMAL(18,2) NOT NULL DEFAULT 0,
    airport_tax DECIMAL(18,2) NOT NULL DEFAULT 0,
    excess_baggage_fee DECIMAL(18,2) NOT NULL DEFAULT 0,
    final_price DECIMAL(18,2) NOT NULL DEFAULT 0,
    ticket_code VARCHAR(30) NULL,
    return_ticket_code VARCHAR(30) NULL,

    CONSTRAINT FK_flight_passengers_booking FOREIGN KEY (flight_booking_id) REFERENCES dbo.flight_bookings(id) ON DELETE CASCADE,
    CONSTRAINT CK_flight_passengers_type CHECK (passenger_type IN ('ADULT', 'CHILD', 'INFANT'))
);
GO

-- Tạo Index tăng tốc truy vấn tìm kiếm chuyến bay và quản lý giữ chỗ
CREATE INDEX IX_flights_search ON dbo.flights(departure_airport, arrival_airport, departure_time);
CREATE INDEX IX_flight_bookings_status_expires ON dbo.flight_bookings(status, hold_expires_at);
CREATE INDEX IX_flight_passengers_booking ON dbo.flight_passengers(flight_booking_id);
GO

-- 4. THÊM DỮ LIỆU MẪU CÁC CHUYẾN BAY (Thời gian trong tương lai gần & xa tính từ cuối tháng 10/2026)
INSERT INTO dbo.flights 
(flight_number, airline, departure_airport, departure_city, arrival_airport, arrival_city, departure_time, arrival_time, base_price, economy_seats_total, economy_seats_available, premium_seats_total, premium_seats_available, business_seats_total, business_seats_available)
VALUES
-- Chặng Hà Nội (HAN) -> TP. Hồ Chí Minh (SGN)
-- Chuyến 1: Dưới 3 ngày (khởi hành sau 1.5 ngày) -> áp dụng hệ số 1.3
('VN215', N'Vietnam Airlines', 'HAN', N'Hà Nội', 'SGN', N'TP. Hồ Chí Minh', '2026-10-29 08:00:00', '2026-10-29 10:15:00', 1400000.00, 150, 142, 30, 28, 16, 14),
('VJ135', N'Vietjet Air', 'HAN', N'Hà Nội', 'SGN', N'TP. Hồ Chí Minh', '2026-10-29 14:30:00', '2026-10-29 16:45:00', 950000.00, 180, 165, 0, 0, 12, 10),

-- Chuyến 2: Từ 3 đến 14 ngày (khởi hành sau 7 ngày) -> áp dụng hệ số 1.1
('VN223', N'Vietnam Airlines', 'HAN', N'Hà Nội', 'SGN', N'TP. Hồ Chí Minh', '2026-11-03 09:30:00', '2026-11-03 11:45:00', 1500000.00, 150, 150, 30, 30, 16, 16),
('QH201', N'Bamboo Airways', 'HAN', N'Hà Nội', 'SGN', N'TP. Hồ Chí Minh', '2026-11-04 18:00:00', '2026-11-04 20:15:00', 1350000.00, 160, 158, 24, 24, 16, 15),

-- Chuyến 3: Trên 14 ngày (khởi hành sau 20 ngày) -> áp dụng hệ số 1.0
('VN245', N'Vietnam Airlines', 'HAN', N'Hà Nội', 'SGN', N'TP. Hồ Chí Minh', '2026-11-16 07:15:00', '2026-11-16 09:30:00', 1450000.00, 150, 150, 30, 30, 16, 16),

-- Chặng Hà Nội (HAN) -> Đà Nẵng (DAD)
('VN165', N'Vietnam Airlines', 'HAN', N'Hà Nội', 'DAD', N'Đà Nẵng', '2026-11-05 10:00:00', '2026-11-05 11:20:00', 1100000.00, 150, 148, 20, 20, 12, 12),
('VJ512', N'Vietjet Air', 'HAN', N'Hà Nội', 'DAD', N'Đà Nẵng', '2026-11-05 15:10:00', '2026-11-05 16:30:00', 850000.00, 180, 180, 0, 0, 8, 8),

-- Chặng TP. Hồ Chí Minh (SGN) -> Phú Quốc (PQC)
('VN1821', N'Vietnam Airlines', 'SGN', N'TP. Hồ Chí Minh', 'PQC', N'Phú Quốc', '2026-11-06 08:45:00', '2026-11-06 09:50:00', 900000.00, 120, 115, 18, 18, 8, 7);
GO
