-- ==========================================================================================
-- BỔ SUNG CẤU TRÚC VÀ DỮ LIỆU CHUYÊN BIỆT CHO PHÂN HỆ VÉ TÀU HỎA (TRAIN MODULE)
-- ==========================================================================================

USE DatVeDuLich;
GO

-- 1. XÓA BẢNG NẾU ĐÃ TỒN TẠI
IF OBJECT_ID('dbo.train_tickets', 'U') IS NOT NULL DROP TABLE dbo.train_tickets;
IF OBJECT_ID('dbo.train_bookings', 'U') IS NOT NULL DROP TABLE dbo.train_bookings;
IF OBJECT_ID('dbo.train_seats', 'U') IS NOT NULL DROP TABLE dbo.train_seats;
IF OBJECT_ID('dbo.train_coaches', 'U') IS NOT NULL DROP TABLE dbo.train_coaches;
IF OBJECT_ID('dbo.train_stations', 'U') IS NOT NULL DROP TABLE dbo.train_stations;
IF OBJECT_ID('dbo.trains', 'U') IS NOT NULL DROP TABLE dbo.trains;
GO

-- 2. TẠO BẢNG trains (Danh mục đoàn tàu)
CREATE TABLE dbo.trains (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    train_code VARCHAR(20) NOT NULL UNIQUE,     -- SE1, SE3, SE5, SE2, SE4
    route_name NVARCHAR(100) NOT NULL,          -- Tuyến Hà Nội - Sài Gòn
    departure_station_name NVARCHAR(100) NOT NULL,
    arrival_station_name NVARCHAR(100) NOT NULL,
    departure_time_of_day VARCHAR(10) NOT NULL, -- 06:00, 19:30, 22:15
    travel_hours INT NOT NULL DEFAULT 32,       -- Thời gian chạy toàn tuyến (tiếng)
    status VARCHAR(20) NOT NULL CONSTRAINT DF_trains_status DEFAULT 'ACTIVE',
    created_at DATETIME2 NOT NULL CONSTRAINT DF_trains_created_at DEFAULT GETDATE()
);
GO

-- 3. TẠO BẢNG train_stations (Danh sách ga theo thứ tự và khoảng cách km trên tuyến)
CREATE TABLE dbo.train_stations (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    route_name NVARCHAR(100) NOT NULL,
    station_code VARCHAR(20) NOT NULL,          -- HANOI, PHULY, NAMDINH, DANANG, SAIGON
    station_name NVARCHAR(100) NOT NULL,        -- Ga Hà Nội, Ga Phủ Lý...
    station_index INT NOT NULL,                 -- Thứ tự ga trên tuyến (0, 1, 2, 3...)
    distance_km INT NOT NULL,                   -- Cự ly km tính từ ga đầu tiên

    CONSTRAINT UQ_train_stations_route_index UNIQUE (route_name, station_index)
);
GO

-- 4. TẠO BẢNG train_coaches (Toa tàu thuộc đoàn tàu)
CREATE TABLE dbo.train_coaches (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    train_id BIGINT NOT NULL,
    coach_number INT NOT NULL,                  -- Toa số 1, 2, 3, 4, 5
    coach_name NVARCHAR(50) NOT NULL,           -- Toa 1 - Ghế Mềm Điều Hòa
    coach_type VARCHAR(30) NOT NULL,            -- SOFT_SEAT, SLEEPER_4, SLEEPER_6
    total_seats INT NOT NULL,
    price_per_km DECIMAL(18,2) NOT NULL,        -- Đơn giá VNĐ / km

    CONSTRAINT FK_train_coaches_train FOREIGN KEY (train_id) REFERENCES dbo.trains(id) ON DELETE CASCADE,
    CONSTRAINT CK_train_coaches_type CHECK (coach_type IN ('SOFT_SEAT', 'SLEEPER_4', 'SLEEPER_6'))
);
GO

-- 5. TẠO BẢNG train_seats (Chỗ ngồi / Giường thuộc toa)
CREATE TABLE dbo.train_seats (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    coach_id BIGINT NOT NULL,
    seat_number INT NOT NULL,                   -- Số ghế 1, 2, 3... 28
    seat_label NVARCHAR(100) NOT NULL,          -- Ghế 1, Giường 1 - Tầng 1...
    position_note NVARCHAR(100) NULL,           -- Cửa sổ, Lối đi, Giường tầng 1...

    CONSTRAINT FK_train_seats_coach FOREIGN KEY (coach_id) REFERENCES dbo.train_coaches(id) ON DELETE CASCADE,
    CONSTRAINT UQ_train_seats_coach_number UNIQUE (coach_id, seat_number)
);
GO

-- 6. TẠO BẢNG train_bookings (Đơn đặt vé tàu)
CREATE TABLE dbo.train_bookings (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    booking_code VARCHAR(30) NOT NULL UNIQUE,
    train_id BIGINT NOT NULL,
    departure_station_id BIGINT NOT NULL,
    arrival_station_id BIGINT NOT NULL,
    departure_date DATE NOT NULL,
    departure_datetime DATETIME2 NOT NULL,
    customer_name NVARCHAR(100) NOT NULL,
    customer_phone VARCHAR(20) NOT NULL,
    customer_email VARCHAR(100) NOT NULL,
    ticket_count INT NOT NULL DEFAULT 1,
    total_original_fare DECIMAL(18,2) NOT NULL DEFAULT 0,
    total_discount DECIMAL(18,2) NOT NULL DEFAULT 0,
    total_amount DECIMAL(18,2) NOT NULL DEFAULT 0,
    refund_amount DECIMAL(18,2) NOT NULL DEFAULT 0,
    cancellation_fee DECIMAL(18,2) NOT NULL DEFAULT 0,
    status VARCHAR(30) NOT NULL CONSTRAINT DF_train_bookings_status DEFAULT 'HELD', -- HELD, CONFIRMED, ISSUED, CANCELLED, EXPIRED
    hold_expires_at DATETIME2 NOT NULL,         -- Giữ chỗ 10 phút
    booking_time DATETIME2 NOT NULL CONSTRAINT DF_train_bookings_booking_time DEFAULT GETDATE(),
    confirmed_time DATETIME2 NULL,
    cancelled_time DATETIME2 NULL,
    cancel_reason NVARCHAR(255) NULL,

    CONSTRAINT FK_train_bookings_train FOREIGN KEY (train_id) REFERENCES dbo.trains(id),
    CONSTRAINT FK_train_bookings_dep_station FOREIGN KEY (departure_station_id) REFERENCES dbo.train_stations(id),
    CONSTRAINT FK_train_bookings_arr_station FOREIGN KEY (arrival_station_id) REFERENCES dbo.train_stations(id),
    CONSTRAINT CK_train_bookings_status CHECK (status IN ('HELD', 'CONFIRMED', 'ISSUED', 'CANCELLED', 'EXPIRED'))
);
GO

-- 7. TẠO BẢNG train_tickets (Vé chi tiết từng hành khách và phân đoạn chặng ga)
CREATE TABLE dbo.train_tickets (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    train_booking_id BIGINT NOT NULL,
    seat_id BIGINT NOT NULL,
    passenger_name NVARCHAR(100) NOT NULL,
    passenger_id_card VARCHAR(20) NULL,
    priority_category VARCHAR(30) NOT NULL CONSTRAINT DF_train_tickets_priority DEFAULT 'STANDARD', -- STANDARD, CHILD_6_10, STUDENT, SENIOR
    priority_doc_id VARCHAR(50) NULL,           -- Mã SV, CCCD người già, Giấy khai sinh trẻ em
    departure_station_index INT NOT NULL,       -- Điểm bắt đầu phân đoạn [start, end)
    arrival_station_index INT NOT NULL,         -- Điểm kết thúc phân đoạn [start, end)
    distance_km INT NOT NULL,
    original_fare DECIMAL(18,2) NOT NULL DEFAULT 0,
    discount_amount DECIMAL(18,2) NOT NULL DEFAULT 0,
    final_fare DECIMAL(18,2) NOT NULL DEFAULT 0,
    ticket_code VARCHAR(30) NULL,

    CONSTRAINT FK_train_tickets_booking FOREIGN KEY (train_booking_id) REFERENCES dbo.train_bookings(id) ON DELETE CASCADE,
    CONSTRAINT FK_train_tickets_seat FOREIGN KEY (seat_id) REFERENCES dbo.train_seats(id),
    CONSTRAINT CK_train_tickets_priority CHECK (priority_category IN ('STANDARD', 'CHILD_6_10', 'STUDENT', 'SENIOR'))
);
GO

-- Index phục vụ thuật toán kiểm tra chồng lấn chặng cực nhanh
CREATE INDEX IX_train_tickets_check_overlap ON dbo.train_tickets(seat_id, departure_station_index, arrival_station_index);
CREATE INDEX IX_train_bookings_date_status ON dbo.train_bookings(train_id, departure_date, status);
GO

-- ==========================================================================================
-- DỮ LIỆU MẪU: ĐOÀN TÀU, GA TÀU, TOA VÀ SƠ ĐỒ CHỖ NGỒI
-- ==========================================================================================

-- 1. Tuyến Đường Sắt Bắc - Nam & Danh sách Ga theo thứ tự (Hà Nội -> Sài Gòn)
INSERT INTO dbo.train_stations (route_name, station_code, station_name, station_index, distance_km)
VALUES
(N'Hà Nội - Sài Gòn', 'HAN', N'Ga Hà Nội', 0, 0),
(N'Hà Nội - Sài Gòn', 'NDI', N'Ga Nam Định', 1, 87),
(N'Hà Nội - Sài Gòn', 'NBI', N'Ga Ninh Bình', 2, 115),
(N'Hà Nội - Sài Gòn', 'THO', N'Ga Thanh Hóa', 3, 176),
(N'Hà Nội - Sài Gòn', 'VIN', N'Ga Vinh', 4, 319),
(N'Hà Nội - Sài Gòn', 'DHO', N'Ga Đồng Hới', 5, 522),
(N'Hà Nội - Sài Gòn', 'HUE', N'Ga Huế', 6, 688),
(N'Hà Nội - Sài Gòn', 'DAN', N'Ga Đà Nẵng', 7, 791),
(N'Hà Nội - Sài Gòn', 'NTR', N'Ga Nha Trang', 8, 1315),
(N'Hà Nội - Sài Gòn', 'BTH', N'Ga Bình Thuận', 9, 1551),
(N'Hà Nội - Sài Gòn', 'SGO', N'Ga Sài Gòn', 10, 1726);
GO

-- 2. Thêm các đoàn tàu Bắc - Nam
INSERT INTO dbo.trains (train_code, route_name, departure_station_name, arrival_station_name, departure_time_of_day, travel_hours, status)
VALUES
('SE1', N'Hà Nội - Sài Gòn', N'Ga Hà Nội', N'Ga Sài Gòn', '06:00', 31, 'ACTIVE'),
('SE3', N'Hà Nội - Sài Gòn', N'Ga Hà Nội', N'Ga Sài Gòn', '19:25', 30, 'ACTIVE'),
('SE5', N'Hà Nội - Sài Gòn', N'Ga Hà Nội', N'Ga Sài Gòn', '15:20', 33, 'ACTIVE');
GO

-- 3. Thêm Toa tàu cho tàu SE1, SE3, SE5 (Toa 1: Ghế mềm 700đ/km, Toa 2: Giường 6 chỗ 1000đ/km, Toa 3: Giường 4 chỗ 1300đ/km)
INSERT INTO dbo.train_coaches (train_id, coach_number, coach_name, coach_type, total_seats, price_per_km)
VALUES
(1, 1, N'Toa 1 - Ghế Mềm Điều Hòa', 'SOFT_SEAT', 16, 700.00),
(1, 2, N'Toa 2 - Giường Nằm Khoang 6', 'SLEEPER_6', 12, 1000.00),
(1, 3, N'Toa 3 - Giường Nằm Khoang 4 VIP', 'SLEEPER_4', 8, 1300.00),

-- Toa tàu cho tàu SE3
(2, 1, N'Toa 1 - Ghế Mềm Điều Hòa', 'SOFT_SEAT', 16, 700.00),
(2, 2, N'Toa 2 - Giường Nằm Khoang 6', 'SLEEPER_6', 12, 1000.00),
(2, 3, N'Toa 3 - Giường Nằm Khoang 4 VIP', 'SLEEPER_4', 8, 1300.00),

-- Toa tàu cho tàu SE5
(3, 1, N'Toa 1 - Ghế Mềm Điều Hòa', 'SOFT_SEAT', 16, 700.00),
(3, 2, N'Toa 2 - Giường Nằm Khoang 6', 'SLEEPER_6', 12, 1000.00),
(3, 3, N'Toa 3 - Giường Nằm Khoang 4 VIP', 'SLEEPER_4', 8, 1300.00);
GO

-- 4. Thêm Chỗ ngồi (Seats) cho Toa 1 của SE1 (16 ghế mềm)
INSERT INTO dbo.train_seats (coach_id, seat_number, seat_label, position_note)
VALUES
(1, 1, 'Ghế 01', N'Cửa sổ'), (1, 2, 'Ghế 02', N'Lối đi'),
(1, 3, 'Ghế 03', N'Lối đi'), (1, 4, 'Ghế 04', N'Cửa sổ'),
(1, 5, 'Ghế 05', N'Cửa sổ'), (1, 6, 'Ghế 06', N'Lối đi'),
(1, 7, 'Ghế 07', N'Lối đi'), (1, 8, 'Ghế 08', N'Cửa sổ'),
(1, 9, 'Ghế 09', N'Cửa sổ'), (1, 10, 'Ghế 10', N'Lối đi'),
(1, 11, 'Ghế 11', N'Lối đi'), (1, 12, 'Ghế 12', N'Cửa sổ'),
(1, 13, 'Ghế 13', N'Cửa sổ'), (1, 14, 'Ghế 14', N'Lối đi'),
(1, 15, 'Ghế 15', N'Lối đi'), (1, 16, 'Ghế 16', N'Cửa sổ');

-- Chỗ nằm cho Toa 2 của SE1 (12 giường khoang 6)
INSERT INTO dbo.train_seats (coach_id, seat_number, seat_label, position_note)
VALUES
(2, 1, 'Khoang 1 - Giường 01 (T1)', N'Tầng 1 - Thấp'),
(2, 2, 'Khoang 1 - Giường 02 (T1)', N'Tầng 1 - Thấp'),
(2, 3, 'Khoang 1 - Giường 03 (T2)', N'Tầng 2 - Giữa'),
(2, 4, 'Khoang 1 - Giường 04 (T2)', N'Tầng 2 - Giữa'),
(2, 5, 'Khoang 1 - Giường 05 (T3)', N'Tầng 3 - Cao'),
(2, 6, 'Khoang 1 - Giường 06 (T3)', N'Tầng 3 - Cao'),
(2, 7, 'Khoang 2 - Giường 07 (T1)', N'Tầng 1 - Thấp'),
(2, 8, 'Khoang 2 - Giường 08 (T1)', N'Tầng 1 - Thấp'),
(2, 9, 'Khoang 2 - Giường 09 (T2)', N'Tầng 2 - Giữa'),
(2, 10, 'Khoang 2 - Giường 10 (T2)', N'Tầng 2 - Giữa'),
(2, 11, 'Khoang 2 - Giường 11 (T3)', N'Tầng 3 - Cao'),
(2, 12, 'Khoang 2 - Giường 12 (T3)', N'Tầng 3 - Cao');

-- Chỗ nằm cho Toa 3 của SE1 (8 giường khoang 4 VIP)
INSERT INTO dbo.train_seats (coach_id, seat_number, seat_label, position_note)
VALUES
(3, 1, 'Khoang 1 - Giường 01 (T1)', N'Tầng 1 VIP'),
(3, 2, 'Khoang 1 - Giường 02 (T1)', N'Tầng 1 VIP'),
(3, 3, 'Khoang 1 - Giường 03 (T2)', N'Tầng 2 VIP'),
(3, 4, 'Khoang 1 - Giường 04 (T2)', N'Tầng 2 VIP'),
(3, 5, 'Khoang 2 - Giường 05 (T1)', N'Tầng 1 VIP'),
(3, 6, 'Khoang 2 - Giường 06 (T1)', N'Tầng 1 VIP'),
(3, 7, 'Khoang 2 - Giường 07 (T2)', N'Tầng 2 VIP'),
(3, 8, 'Khoang 2 - Giường 08 (T2)', N'Tầng 2 VIP');

-- Chỗ ngồi cho SE3 Toa 1 (16 ghế mềm)
INSERT INTO dbo.train_seats (coach_id, seat_number, seat_label, position_note)
VALUES
(4, 1, 'Ghế 01', N'Cửa sổ'), (4, 2, 'Ghế 02', N'Lối đi'),
(4, 3, 'Ghế 03', N'Lối đi'), (4, 4, 'Ghế 04', N'Cửa sổ'),
(4, 5, 'Ghế 05', N'Cửa sổ'), (4, 6, 'Ghế 06', N'Lối đi'),
(4, 7, 'Ghế 07', N'Lối đi'), (4, 8, 'Ghế 08', N'Cửa sổ'),
(4, 9, 'Ghế 09', N'Cửa sổ'), (4, 10, 'Ghế 10', N'Lối đi'),
(4, 11, 'Ghế 11', N'Lối đi'), (4, 12, 'Ghế 12', N'Cửa sổ'),
(4, 13, 'Ghế 13', N'Cửa sổ'), (4, 14, 'Ghế 14', N'Lối đi'),
(4, 15, 'Ghế 15', N'Lối đi'), (4, 16, 'Ghế 16', N'Cửa sổ');

-- Chỗ nằm cho SE3 Toa 2 (12 giường khoang 6)
INSERT INTO dbo.train_seats (coach_id, seat_number, seat_label, position_note)
VALUES
(5, 1, 'Khoang 1 - Giường 01 (T1)', N'Tầng 1 - Thấp'),
(5, 2, 'Khoang 1 - Giường 02 (T1)', N'Tầng 1 - Thấp'),
(5, 3, 'Khoang 1 - Giường 03 (T2)', N'Tầng 2 - Giữa'),
(5, 4, 'Khoang 1 - Giường 04 (T2)', N'Tầng 2 - Giữa'),
(5, 5, 'Khoang 1 - Giường 05 (T3)', N'Tầng 3 - Cao'),
(5, 6, 'Khoang 1 - Giường 06 (T3)', N'Tầng 3 - Cao'),
(5, 7, 'Khoang 2 - Giường 07 (T1)', N'Tầng 1 - Thấp'),
(5, 8, 'Khoang 2 - Giường 08 (T1)', N'Tầng 1 - Thấp'),
(5, 9, 'Khoang 2 - Giường 09 (T2)', N'Tầng 2 - Giữa'),
(5, 10, 'Khoang 2 - Giường 10 (T2)', N'Tầng 2 - Giữa'),
(5, 11, 'Khoang 2 - Giường 11 (T3)', N'Tầng 3 - Cao'),
(5, 12, 'Khoang 2 - Giường 12 (T3)', N'Tầng 3 - Cao');

-- Chỗ nằm cho SE3 Toa 3 (8 giường khoang 4 VIP)
INSERT INTO dbo.train_seats (coach_id, seat_number, seat_label, position_note)
VALUES
(6, 1, 'Khoang 1 - Giường 01 (T1)', N'Tầng 1 VIP'),
(6, 2, 'Khoang 1 - Giường 02 (T1)', N'Tầng 1 VIP'),
(6, 3, 'Khoang 1 - Giường 03 (T2)', N'Tầng 2 VIP'),
(6, 4, 'Khoang 1 - Giường 04 (T2)', N'Tầng 2 VIP'),
(6, 5, 'Khoang 2 - Giường 05 (T1)', N'Tầng 1 VIP'),
(6, 6, 'Khoang 2 - Giường 06 (T1)', N'Tầng 1 VIP'),
(6, 7, 'Khoang 2 - Giường 07 (T2)', N'Tầng 2 VIP'),
(6, 8, 'Khoang 2 - Giường 08 (T2)', N'Tầng 2 VIP');

-- Chỗ ngồi cho SE5 Toa 1 (16 ghế mềm)
INSERT INTO dbo.train_seats (coach_id, seat_number, seat_label, position_note)
VALUES
(7, 1, 'Ghế 01', N'Cửa sổ'), (7, 2, 'Ghế 02', N'Lối đi'),
(7, 3, 'Ghế 03', N'Lối đi'), (7, 4, 'Ghế 04', N'Cửa sổ'),
(7, 5, 'Ghế 05', N'Cửa sổ'), (7, 6, 'Ghế 06', N'Lối đi'),
(7, 7, 'Ghế 07', N'Lối đi'), (7, 8, 'Ghế 08', N'Cửa sổ'),
(7, 9, 'Ghế 09', N'Cửa sổ'), (7, 10, 'Ghế 10', N'Lối đi'),
(7, 11, 'Ghế 11', N'Lối đi'), (7, 12, 'Ghế 12', N'Cửa sổ'),
(7, 13, 'Ghế 13', N'Cửa sổ'), (7, 14, 'Ghế 14', N'Lối đi'),
(7, 15, 'Ghế 15', N'Lối đi'), (7, 16, 'Ghế 16', N'Cửa sổ');

-- Chỗ nằm cho SE5 Toa 2 (12 giường khoang 6)
INSERT INTO dbo.train_seats (coach_id, seat_number, seat_label, position_note)
VALUES
(8, 1, 'Khoang 1 - Giường 01 (T1)', N'Tầng 1 - Thấp'),
(8, 2, 'Khoang 1 - Giường 02 (T1)', N'Tầng 1 - Thấp'),
(8, 3, 'Khoang 1 - Giường 03 (T2)', N'Tầng 2 - Giữa'),
(8, 4, 'Khoang 1 - Giường 04 (T2)', N'Tầng 2 - Giữa'),
(8, 5, 'Khoang 1 - Giường 05 (T3)', N'Tầng 3 - Cao'),
(8, 6, 'Khoang 1 - Giường 06 (T3)', N'Tầng 3 - Cao'),
(8, 7, 'Khoang 2 - Giường 07 (T1)', N'Tầng 1 - Thấp'),
(8, 8, 'Khoang 2 - Giường 08 (T1)', N'Tầng 1 - Thấp'),
(8, 9, 'Khoang 2 - Giường 09 (T2)', N'Tầng 2 - Giữa'),
(8, 10, 'Khoang 2 - Giường 10 (T2)', N'Tầng 2 - Giữa'),
(8, 11, 'Khoang 2 - Giường 11 (T3)', N'Tầng 3 - Cao'),
(8, 12, 'Khoang 2 - Giường 12 (T3)', N'Tầng 3 - Cao');

-- Chỗ nằm cho SE5 Toa 3 (8 giường khoang 4 VIP)
INSERT INTO dbo.train_seats (coach_id, seat_number, seat_label, position_note)
VALUES
(9, 1, 'Khoang 1 - Giường 01 (T1)', N'Tầng 1 VIP'),
(9, 2, 'Khoang 1 - Giường 02 (T1)', N'Tầng 1 VIP'),
(9, 3, 'Khoang 1 - Giường 03 (T2)', N'Tầng 2 VIP'),
(9, 4, 'Khoang 1 - Giường 04 (T2)', N'Tầng 2 VIP'),
(9, 5, 'Khoang 2 - Giường 05 (T1)', N'Tầng 1 VIP'),
(9, 6, 'Khoang 2 - Giường 06 (T1)', N'Tầng 1 VIP'),
(9, 7, 'Khoang 2 - Giường 07 (T2)', N'Tầng 2 VIP'),
(9, 8, 'Khoang 2 - Giường 08 (T2)', N'Tầng 2 VIP');
GO
