-- ==========================================================================================
-- HỆ THỐNG QUẢN LÝ ĐẶT VÉ DU LỊCH (TRAVEL TICKET BOOKING & MANAGEMENT PORTAL)
-- CSDL: Microsoft SQL Server (DatVeDuLich)
-- CẤU TRÚC: CHUẨN 4 BẢNG CỐT LÕI (NguoiDung, DonDatCho, Ve, GiaoDich)
-- Lịch hẹn tư vấn được tích hợp trực tiếp vào Đơn Đặt Chỗ (chỉ xuất hiện sau khi đặt vé xong)
-- Toàn bộ tên bảng và trường thuộc tính chuẩn hóa 100% Tiếng Việt
-- ==========================================================================================

USE DatVeDuLich;
GO

-- 1. XÓA BẢNG NẾU ĐÃ TỒN TẠI (Theo thứ tự khóa ngoại, dọn sạch cả bảng snake_case)
IF OBJECT_ID('dbo.giao_dich', 'U') IS NOT NULL DROP TABLE dbo.giao_dich;
IF OBJECT_ID('dbo.GiaoDich', 'U') IS NOT NULL DROP TABLE dbo.GiaoDich;
IF OBJECT_ID('dbo.trans_details', 'U') IS NOT NULL DROP TABLE dbo.trans_details;
IF OBJECT_ID('dbo.payments', 'U') IS NOT NULL DROP TABLE dbo.payments;

IF OBJECT_ID('dbo.Ve', 'U') IS NOT NULL DROP TABLE dbo.Ve;
IF OBJECT_ID('dbo.tickets', 'U') IS NOT NULL DROP TABLE dbo.tickets;
IF OBJECT_ID('dbo.ticket_details', 'U') IS NOT NULL DROP TABLE dbo.ticket_details;
IF OBJECT_ID('dbo.flight_passengers', 'U') IS NOT NULL DROP TABLE dbo.flight_passengers;
IF OBJECT_ID('dbo.train_tickets', 'U') IS NOT NULL DROP TABLE dbo.train_tickets;

IF OBJECT_ID('dbo.lich_hen', 'U') IS NOT NULL DROP TABLE dbo.lich_hen;
IF OBJECT_ID('dbo.LichHen', 'U') IS NOT NULL DROP TABLE dbo.LichHen;
IF OBJECT_ID('dbo.appointments', 'U') IS NOT NULL DROP TABLE dbo.appointments;
IF OBJECT_ID('dbo.consultations', 'U') IS NOT NULL DROP TABLE dbo.consultations;

IF OBJECT_ID('dbo.don_dat_cho', 'U') IS NOT NULL DROP TABLE dbo.don_dat_cho;
IF OBJECT_ID('dbo.DonDatCho', 'U') IS NOT NULL DROP TABLE dbo.DonDatCho;
IF OBJECT_ID('dbo.reservations', 'U') IS NOT NULL DROP TABLE dbo.reservations;
IF OBJECT_ID('dbo.bookings', 'U') IS NOT NULL DROP TABLE dbo.bookings;
IF OBJECT_ID('dbo.flight_bookings', 'U') IS NOT NULL DROP TABLE dbo.flight_bookings;
IF OBJECT_ID('dbo.train_bookings', 'U') IS NOT NULL DROP TABLE dbo.train_bookings;

IF OBJECT_ID('dbo.nguoi_dung', 'U') IS NOT NULL DROP TABLE dbo.nguoi_dung;
IF OBJECT_ID('dbo.NguoiDung', 'U') IS NOT NULL DROP TABLE dbo.NguoiDung;
IF OBJECT_ID('dbo.users', 'U') IS NOT NULL DROP TABLE dbo.users;
IF OBJECT_ID('dbo.customers', 'U') IS NOT NULL DROP TABLE dbo.customers;
GO

-- ------------------------------------------------------------------------------------------
-- BẢNG 1: NguoiDung (Tài khoản người dùng, phân quyền 4 vai trò RBAC)
-- ------------------------------------------------------------------------------------------
CREATE TABLE dbo.NguoiDung (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    ten_dang_nhap VARCHAR(50) NOT NULL UNIQUE,
    mat_khau VARCHAR(255) NOT NULL,
    ho_ten NVARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    so_dien_thoai VARCHAR(20) NULL,
    vai_tro VARCHAR(30) NOT NULL CONSTRAINT DF_NguoiDung_vai_tro DEFAULT 'ROLE_TRAVELER',
    trang_thai BIT NOT NULL CONSTRAINT DF_NguoiDung_trang_thai DEFAULT 1,
    ngay_tao DATETIME2 NOT NULL CONSTRAINT DF_NguoiDung_ngay_tao DEFAULT GETDATE(),

    CONSTRAINT CK_NguoiDung_vai_tro CHECK (vai_tro IN ('ROLE_TRAVELER', 'ROLE_AGENT', 'ROLE_OPERATOR', 'ROLE_ADMIN'))
);
GO

-- ------------------------------------------------------------------------------------------
-- BẢNG 2: DonDatCho (Đơn đặt giữ chỗ cho Vé Máy Bay & Vé Tàu Hỏa, tích hợp Lịch Hẹn Tư Vấn)
-- Lịch hẹn tư vấn chỉ xuất hiện sau khi khách đặt vé thành công, tinh gọn CSDL xuống 4 bảng
-- ------------------------------------------------------------------------------------------
CREATE TABLE dbo.DonDatCho (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_dat_cho VARCHAR(30) NOT NULL UNIQUE,
    nguoi_dung_id BIGINT NULL,
    ten_khach_hang NVARCHAR(100) NOT NULL,
    so_dien_thoai VARCHAR(20) NOT NULL,
    email VARCHAR(100) NOT NULL,
    loai_phuong_tien VARCHAR(20) NOT NULL,
    diem_di NVARCHAR(100) NOT NULL,
    diem_den NVARCHAR(100) NOT NULL,
    ngay_di DATETIME2 NOT NULL,
    ngay_ve DATETIME2 NULL,
    la_khu_hoi BIT NOT NULL CONSTRAINT DF_DonDatCho_khu_hoi DEFAULT 0,
    so_luong_khach INT NOT NULL CONSTRAINT DF_DonDatCho_so_khach DEFAULT 1,
    tong_tien DECIMAL(18,2) NOT NULL CONSTRAINT DF_DonDatCho_tong_tien DEFAULT 0,
    trang_thai VARCHAR(20) NOT NULL CONSTRAINT DF_DonDatCho_trang_thai DEFAULT 'CREATED',
    han_giu_cho DATETIME2 NOT NULL,
    ly_do_huy NVARCHAR(255) NULL,

    -- Thuộc tính Tích hợp Lịch Hẹn Tư Vấn sau khi đặt vé
    co_hen_tu_van BIT NOT NULL CONSTRAINT DF_DonDatCho_co_hen DEFAULT 0,
    ngay_hen_tu_van DATETIME2 NULL,
    ghi_chu_tu_van NVARCHAR(500) NULL,
    trang_thai_tu_van VARCHAR(20) NOT NULL CONSTRAINT DF_DonDatCho_tt_tu_van DEFAULT 'NONE',
    nhan_vien_tu_van_id BIGINT NULL,

    ngay_tao DATETIME2 NOT NULL CONSTRAINT DF_DonDatCho_ngay_tao DEFAULT GETDATE(),
    ngay_cap_nhat DATETIME2 NULL,

    CONSTRAINT FK_DonDatCho_NguoiDung FOREIGN KEY (nguoi_dung_id) REFERENCES dbo.NguoiDung(id),
    CONSTRAINT CK_DonDatCho_phuong_tien CHECK (loai_phuong_tien IN ('AIR', 'TRAIN')),
    CONSTRAINT CK_DonDatCho_trang_thai CHECK (trang_thai IN ('CREATED', 'CONFIRMED', 'CANCELLED', 'EXPIRED')),
    CONSTRAINT CK_DonDatCho_trang_thai_tu_van CHECK (trang_thai_tu_van IN ('NONE', 'PENDING', 'CONFIRMED', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT CK_DonDatCho_so_khach CHECK (so_luong_khach >= 1)
);
GO

-- ------------------------------------------------------------------------------------------
-- BẢNG 3: Ve (Kho vé khả dụng & Chi tiết vé/ghế đã đặt của từng hành khách)
-- ------------------------------------------------------------------------------------------
CREATE TABLE dbo.Ve (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    don_dat_cho_id BIGINT NULL,
    loai_phuong_tien VARCHAR(20) NOT NULL,
    so_hieu_chuyen VARCHAR(50) NOT NULL,
    ten_phuong_tien NVARCHAR(100) NOT NULL,
    tuyen_duong NVARCHAR(200) NOT NULL,
    diem_di NVARCHAR(100) NOT NULL,
    diem_den NVARCHAR(100) NOT NULL,
    gio_di DATETIME2 NOT NULL,
    gio_den DATETIME2 NOT NULL,
    so_ghe NVARCHAR(50) NOT NULL,
    hang_ve VARCHAR(50) NOT NULL,
    ten_hanh_khach NVARCHAR(100) NULL,
    so_cccd VARCHAR(30) NULL,
    loai_khach VARCHAR(20) NOT NULL CONSTRAINT DF_Ve_loai_khach DEFAULT 'ADULT',
    gia_goc DECIMAL(18,2) NOT NULL DEFAULT 0,
    phi_dich_vu DECIMAL(18,2) NOT NULL DEFAULT 0,
    phu_phi DECIMAL(18,2) NOT NULL DEFAULT 0,
    gia_cuoi DECIMAL(18,2) NOT NULL DEFAULT 0,
    ma_ve_dien_tu VARCHAR(50) NULL,
    du_lieu_qr NVARCHAR(MAX) NULL,
    trang_thai VARCHAR(20) NOT NULL CONSTRAINT DF_Ve_trang_thai DEFAULT 'AVAILABLE',
    ngay_tao DATETIME2 NOT NULL CONSTRAINT DF_Ve_ngay_tao DEFAULT GETDATE(),

    CONSTRAINT FK_Ve_DonDatCho FOREIGN KEY (don_dat_cho_id) REFERENCES dbo.DonDatCho(id) ON DELETE SET NULL,
    CONSTRAINT CK_Ve_phuong_tien CHECK (loai_phuong_tien IN ('AIR', 'TRAIN')),
    CONSTRAINT CK_Ve_loai_khach CHECK (loai_khach IN ('ADULT', 'CHILD', 'INFANT', 'SENIOR', 'STUDENT')),
    CONSTRAINT CK_Ve_trang_thai CHECK (trang_thai IN ('AVAILABLE', 'HELD', 'ISSUED', 'CANCELLED'))
);
GO

-- ------------------------------------------------------------------------------------------
-- BẢNG 4: GiaoDich (Lịch sử giao dịch thanh toán & đối soát tài chính)
-- ------------------------------------------------------------------------------------------
CREATE TABLE dbo.GiaoDich (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    don_dat_cho_id BIGINT NOT NULL,
    ma_giao_dich VARCHAR(50) NOT NULL UNIQUE,
    so_tien DECIMAL(18,2) NOT NULL,
    phuong_thuc VARCHAR(30) NOT NULL,
    trang_thai VARCHAR(20) NOT NULL CONSTRAINT DF_GiaoDich_trang_thai DEFAULT 'SUCCESS',
    ma_tham_chieu VARCHAR(100) NULL,
    ghi_chu NVARCHAR(255) NULL,
    thoi_gian_giao_dich DATETIME2 NOT NULL CONSTRAINT DF_GiaoDich_thoi_gian DEFAULT GETDATE(),

    CONSTRAINT FK_GiaoDich_DonDatCho FOREIGN KEY (don_dat_cho_id) REFERENCES dbo.DonDatCho(id) ON DELETE CASCADE,
    CONSTRAINT CK_GiaoDich_phuong_thuc CHECK (phuong_thuc IN ('VNPAY', 'BANK_TRANSFER', 'CREDIT_CARD', 'CASH')),
    CONSTRAINT CK_GiaoDich_trang_thai CHECK (trang_thai IN ('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED'))
);
GO

-- ------------------------------------------------------------------------------------------
-- TẠO CHỈ MỤC (INDEXES) TỐI ƯU HIỆU NĂNG
-- ------------------------------------------------------------------------------------------
CREATE INDEX IX_DonDatCho_ma_dat_cho ON dbo.DonDatCho(ma_dat_cho);
CREATE INDEX IX_DonDatCho_nguoi_dung_id ON dbo.DonDatCho(nguoi_dung_id);
CREATE INDEX IX_DonDatCho_so_dien_thoai ON dbo.DonDatCho(so_dien_thoai);
CREATE INDEX IX_DonDatCho_trang_thai ON dbo.DonDatCho(trang_thai);
CREATE INDEX IX_DonDatCho_tu_van ON dbo.DonDatCho(co_hen_tu_van, trang_thai_tu_van);

CREATE INDEX IX_Ve_don_dat_cho_id ON dbo.Ve(don_dat_cho_id);
CREATE INDEX IX_Ve_tim_kiem ON dbo.Ve(loai_phuong_tien, diem_di, diem_den, gio_di, trang_thai);
CREATE INDEX IX_Ve_ma_ve_dien_tu ON dbo.Ve(ma_ve_dien_tu);

CREATE INDEX IX_GiaoDich_don_dat_cho_id ON dbo.GiaoDich(don_dat_cho_id);
CREATE INDEX IX_GiaoDich_ma_giao_dich ON dbo.GiaoDich(ma_giao_dich);
GO
