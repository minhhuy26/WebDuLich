-- ==========================================================================================
-- HỆ THỐNG QUẢN LÝ ĐẶT VÉ DU LỊCH (TRAVEL TICKET BOOKING & MANAGEMENT PORTAL)
-- DỮ LIỆU MẪU CSDL SQL SERVER (4 BẢNG CỐT LÕI - 100% TIẾNG VIỆT)
-- Lịch hẹn tư vấn được gắn liền với đơn đặt vé (xuất hiện sau khi đặt vé xong)
-- ==========================================================================================

USE DatVeDuLich;
GO

-- 0. DỌN DẸP DỮ LIỆU CŨ THEO THỨ TỰ KHÓA NGOẠI (GIỮ IDEMPOTENT)
DELETE FROM dbo.GiaoDich;
DELETE FROM dbo.Ve;
DELETE FROM dbo.DonDatCho;
DELETE FROM dbo.NguoiDung;
GO

-- 1. TÀI KHOẢN NGƯỜI DÙNG (4 VAI TRÒ CHUẨN RBAC - CỐ ĐỊNH ID 1 ĐẾN 4)
SET IDENTITY_INSERT dbo.NguoiDung ON;
INSERT INTO dbo.NguoiDung (id, ten_dang_nhap, mat_khau, ho_ten, email, so_dien_thoai, vai_tro, trang_thai, ngay_tao)
VALUES
(1, 'admin', '$2a$10$6cmGlTKrQWyzEGgNky/4VeTzJwXNapAHF4zUgYVlrW4XgUiSsutRy', N'Quản Trị Viên Hệ Thống', 'admin@dulich.vn', '0901000001', 'ROLE_ADMIN', 1, GETDATE()),
(2, 'operator_mai', '$2a$10$UZHylGdYv7oQf0XvjHV/8eZxQ8UZPU4AwgOTmND0TCm72kNq0Eu42', N'Nguyễn Thị Mai (Điều hành vé)', 'mai.operator@dulich.vn', '0902000002', 'ROLE_OPERATOR', 1, GETDATE()),
(3, 'agent_saigontour', '$2a$10$Xe0a/RzLD.Lujld9jwFYOeZLV4H6pX9pP42sV/.WYt4ykc.XXBzz.', N'Đại Lý Du Lịch Sài Gòn', 'contact@saigontour.vn', '0903000003', 'ROLE_AGENT', 1, GETDATE()),
(4, 'traveler_an', '$2a$10$13iuAPqzfVKeyXbSlJgewOMkUO21XbxDtRp1u2SX7zFeC9tJEcXhm', N'Trần Văn An (Du khách)', 'an.tran@gmail.com', '0912345678', 'ROLE_TRAVELER', 1, GETDATE());
SET IDENTITY_INSERT dbo.NguoiDung OFF;
GO

-- 2. KHO VÉ & LỊCH TRÌNH 2 LOẠI PHƯƠNG TIỆN (Ve)
-- 2.1. VÉ MÁY BAY (AIR) - Vietnam Airlines, Vietjet Air, Bamboo Airways
INSERT INTO dbo.Ve (loai_phuong_tien, so_hieu_chuyen, ten_phuong_tien, tuyen_duong, diem_di, diem_den, gio_di, gio_den, so_ghe, hang_ve, ten_hanh_khach, so_cccd, loai_khach, gia_goc, phi_dich_vu, phu_phi, gia_cuoi, ma_ve_dien_tu, du_lieu_qr, trang_thai, ngay_tao)
VALUES
('AIR', 'VN210', N'Vietnam Airlines Airbus A321', N'Hà Nội (HAN) ➔ TP. Hồ Chí Minh (SGN)', N'Hà Nội (HAN)', N'TP. Hồ Chí Minh (SGN)', DATEADD(day, 3, GETDATE()), DATEADD(minute, 130, DATEADD(day, 3, GETDATE())), '12A', 'ECONOMY', NULL, NULL, 'ADULT', 1500000, 120000, 0, 1620000, NULL, NULL, 'AVAILABLE', GETDATE()),
('AIR', 'VN210', N'Vietnam Airlines Airbus A321', N'Hà Nội (HAN) ➔ TP. Hồ Chí Minh (SGN)', N'Hà Nội (HAN)', N'TP. Hồ Chí Minh (SGN)', DATEADD(day, 3, GETDATE()), DATEADD(minute, 130, DATEADD(day, 3, GETDATE())), '12B', 'ECONOMY', NULL, NULL, 'ADULT', 1500000, 120000, 0, 1620000, NULL, NULL, 'AVAILABLE', GETDATE()),
('AIR', 'VN210', N'Vietnam Airlines Airbus A321', N'Hà Nội (HAN) ➔ TP. Hồ Chí Minh (SGN)', N'Hà Nội (HAN)', N'TP. Hồ Chí Minh (SGN)', DATEADD(day, 3, GETDATE()), DATEADD(minute, 130, DATEADD(day, 3, GETDATE())), '02A', 'BUSINESS', NULL, NULL, 'ADULT', 3200000, 120000, 0, 3320000, NULL, NULL, 'AVAILABLE', GETDATE()),
('AIR', 'VJ134', N'Vietjet Air Airbus A320', N'Hà Nội (HAN) ➔ Đà Nẵng (DAD)', N'Hà Nội (HAN)', N'Đà Nẵng (DAD)', DATEADD(day, 2, GETDATE()), DATEADD(minute, 80, DATEADD(day, 2, GETDATE())), '15C', 'ECONOMY', NULL, NULL, 'ADULT', 980000, 120000, 0, 1100000, NULL, NULL, 'AVAILABLE', GETDATE()),
('AIR', 'QH203', N'Bamboo Airways Embraer 190', N'Hà Nội (HAN) ➔ Phú Quốc (PQC)', N'Hà Nội (HAN)', N'Phú Quốc (PQC)', DATEADD(day, 5, GETDATE()), DATEADD(minute, 135, DATEADD(day, 5, GETDATE())), '08A', 'ECONOMY', NULL, NULL, 'ADULT', 1850000, 120000, 0, 1970000, NULL, NULL, 'AVAILABLE', GETDATE()),
('AIR', 'VN211', N'Vietnam Airlines Airbus A321', N'Sài Gòn (SGN) ➔ Hà Nội (HAN)', N'Sài Gòn (SGN)', N'Hà Nội (HAN)', DATEADD(day, 2, GETDATE()), DATEADD(minute, 130, DATEADD(day, 2, GETDATE())), '16A', 'ECONOMY', NULL, NULL, 'ADULT', 1550000, 120000, 0, 1670000, NULL, NULL, 'AVAILABLE', GETDATE()),
('AIR', 'VJ135', N'Vietjet Air Airbus A320', N'Đà Nẵng (DAD) ➔ Hà Nội (HAN)', N'Đà Nẵng (DAD)', N'Hà Nội (HAN)', DATEADD(day, 3, GETDATE()), DATEADD(minute, 80, DATEADD(day, 3, GETDATE())), '11B', 'ECONOMY', NULL, NULL, 'ADULT', 950000, 120000, 0, 1070000, NULL, NULL, 'AVAILABLE', GETDATE()),
('AIR', 'QH204', N'Bamboo Airways Embraer 190', N'Phú Quốc (PQC) ➔ Hà Nội (HAN)', N'Phú Quốc (PQC)', N'Hà Nội (HAN)', DATEADD(day, 4, GETDATE()), DATEADD(minute, 135, DATEADD(day, 4, GETDATE())), '07C', 'ECONOMY', NULL, NULL, 'ADULT', 1890000, 120000, 0, 2010000, NULL, NULL, 'AVAILABLE', GETDATE());

-- 2.2. VÉ TÀU HỎA (TRAIN) - Tuyến Thống Nhất Bắc - Nam (SE1, SE2, SE3, SE4, SE5)
INSERT INTO dbo.Ve (loai_phuong_tien, so_hieu_chuyen, ten_phuong_tien, tuyen_duong, diem_di, diem_den, gio_di, gio_den, so_ghe, hang_ve, ten_hanh_khach, so_cccd, loai_khach, gia_goc, phi_dich_vu, phu_phi, gia_cuoi, ma_ve_dien_tu, du_lieu_qr, trang_thai, ngay_tao)
VALUES
('TRAIN', 'SE1', N'Đoàn Tàu Thống Nhất SE1', N'Ga Hà Nội ➔ Ga Đà Nẵng', N'Ga Hà Nội', N'Ga Đà Nẵng', DATEADD(day, 2, GETDATE()), DATEADD(hour, 16, DATEADD(day, 2, GETDATE())), N'Toa 2 - Ghế 15', 'SOFT_SEAT', NULL, NULL, 'ADULT', 580000, 10000, 0, 590000, NULL, NULL, 'AVAILABLE', GETDATE()),
('TRAIN', 'SE1', N'Đoàn Tàu Thống Nhất SE1', N'Ga Hà Nội ➔ Ga Đà Nẵng', N'Ga Hà Nội', N'Ga Đà Nẵng', DATEADD(day, 2, GETDATE()), DATEADD(hour, 16, DATEADD(day, 2, GETDATE())), N'Toa 4 - Giường 11', 'SLEEPER_4', NULL, NULL, 'ADULT', 950000, 10000, 0, 960000, NULL, NULL, 'AVAILABLE', GETDATE()),
('TRAIN', 'SE3', N'Đoàn Tàu Thống Nhất SE3', N'Ga Hà Nội ➔ Ga Sài Gòn', N'Ga Hà Nội', N'Ga Sài Gòn', DATEADD(day, 4, GETDATE()), DATEADD(hour, 30, DATEADD(day, 4, GETDATE())), N'Toa 5 - Giường 08 VIP', 'SLEEPER_4', NULL, NULL, 'ADULT', 1350000, 10000, 0, 1360000, NULL, NULL, 'AVAILABLE', GETDATE()),
('TRAIN', 'SE5', N'Đoàn Tàu Thống Nhất SE5', N'Ga Hà Nội ➔ Ga Huế', N'Ga Hà Nội', N'Ga Huế', DATEADD(day, 3, GETDATE()), DATEADD(hour, 13, DATEADD(day, 3, GETDATE())), N'Toa 3 - Ghế 22', 'SOFT_SEAT', NULL, NULL, 'ADULT', 490000, 10000, 0, 500000, NULL, NULL, 'AVAILABLE', GETDATE()),
('TRAIN', 'SE2', N'Đoàn Tàu Thống Nhất SE2', N'Ga Sài Gòn ➔ Ga Hà Nội', N'Ga Sài Gòn', N'Ga Hà Nội', DATEADD(day, 2, GETDATE()), DATEADD(hour, 30, DATEADD(day, 2, GETDATE())), N'Toa 3 - Giường 09 VIP', 'SLEEPER_4', NULL, NULL, 'ADULT', 1350000, 10000, 0, 1360000, NULL, NULL, 'AVAILABLE', GETDATE()),
('TRAIN', 'SE4', N'Đoàn Tàu Thống Nhất SE4', N'Ga Đà Nẵng ➔ Ga Hà Nội', N'Ga Đà Nẵng', N'Ga Hà Nội', DATEADD(day, 3, GETDATE()), DATEADD(hour, 16, DATEADD(day, 3, GETDATE())), N'Toa 2 - Ghế 18', 'SOFT_SEAT', NULL, NULL, 'ADULT', 580000, 10000, 0, 590000, NULL, NULL, 'AVAILABLE', GETDATE());
GO

-- 3. ĐƠN ĐẶT CHỖ MẪU (TÍCH HỢP LỊCH HẸN TƯ VẤN SAU KHI ĐẶT VÉ)
SET IDENTITY_INSERT dbo.DonDatCho ON;
-- Đơn 1: Đã xác nhận & có yêu cầu tư vấn đã được Chuyên viên Mai (id=2) xác nhận
INSERT INTO dbo.DonDatCho (id, ma_dat_cho, nguoi_dung_id, ten_khach_hang, so_dien_thoai, email, loai_phuong_tien, diem_di, diem_den, ngay_di, ngay_ve, la_khu_hoi, so_luong_khach, tong_tien, trang_thai, han_giu_cho, ly_do_huy, co_hen_tu_van, ngay_hen_tu_van, ghi_chu_tu_van, trang_thai_tu_van, nhan_vien_tu_van_id, ngay_tao)
VALUES
(1, 'RES-202611-0001', 4, N'Trần Văn An', '0912345678', 'an.tran@gmail.com', 'AIR', N'Hà Nội (HAN)', N'TP. Hồ Chí Minh (SGN)', DATEADD(day, 7, GETDATE()), NULL, 0, 1, 1620000, 'CONFIRMED', DATEADD(hour, 1, GETDATE()), NULL, 1, DATEADD(day, 2, GETDATE()), N'Tư vấn thêm hành lý ký gửi quá cước và dịch vụ đón tiễn sân bay', 'CONFIRMED', 2, GETDATE());

-- Đơn 2: Đã xác nhận & có yêu cầu tư vấn đang chờ xử lý (PENDING)
INSERT INTO dbo.DonDatCho (id, ma_dat_cho, nguoi_dung_id, ten_khach_hang, so_dien_thoai, email, loai_phuong_tien, diem_di, diem_den, ngay_di, ngay_ve, la_khu_hoi, so_luong_khach, tong_tien, trang_thai, han_giu_cho, ly_do_huy, co_hen_tu_van, ngay_hen_tu_van, ghi_chu_tu_van, trang_thai_tu_van, nhan_vien_tu_van_id, ngay_tao)
VALUES
(2, 'RES-202611-0002', 3, N'Đại Lý Du Lịch Sài Gòn', '0903000003', 'contact@saigontour.vn', 'TRAIN', N'Ga Hà Nội', N'Ga Sài Gòn', DATEADD(day, 10, GETDATE()), NULL, 0, 1, 1360000, 'CONFIRMED', DATEADD(hour, 1, GETDATE()), NULL, 1, DATEADD(day, 3, GETDATE()), N'Tư vấn xuất hóa đơn VAT điện tử cho đoàn công tác', 'PENDING', NULL, GETDATE());
SET IDENTITY_INSERT dbo.DonDatCho OFF;
GO

-- Cập nhật vé đã xuất gắn vào đơn RES-202611-0001
INSERT INTO dbo.Ve (don_dat_cho_id, loai_phuong_tien, so_hieu_chuyen, ten_phuong_tien, tuyen_duong, diem_di, diem_den, gio_di, gio_den, so_ghe, hang_ve, ten_hanh_khach, so_cccd, loai_khach, gia_goc, phi_dich_vu, phu_phi, gia_cuoi, ma_ve_dien_tu, du_lieu_qr, trang_thai, ngay_tao)
VALUES
(1, 'AIR', 'VN210', N'Vietnam Airlines Airbus A321', N'Hà Nội (HAN) ➔ TP. Hồ Chí Minh (SGN)', N'Hà Nội (HAN)', N'TP. Hồ Chí Minh (SGN)', DATEADD(day, 7, GETDATE()), DATEADD(minute, 130, DATEADD(day, 7, GETDATE())), '14C', 'ECONOMY', N'Trần Văn An', '001200001234', 'ADULT', 1500000, 120000, 0, 1620000, 'ETK-AIR-998822', 'VERIFY:ETK-AIR-998822|RES-202611-0001|VN210|14C|TRAN VAN AN', 'ISSUED', GETDATE());

-- Cập nhật vé đã xuất gắn vào đơn RES-202611-0002
INSERT INTO dbo.Ve (don_dat_cho_id, loai_phuong_tien, so_hieu_chuyen, ten_phuong_tien, tuyen_duong, diem_di, diem_den, gio_di, gio_den, so_ghe, hang_ve, ten_hanh_khach, so_cccd, loai_khach, gia_goc, phi_dich_vu, phu_phi, gia_cuoi, ma_ve_dien_tu, du_lieu_qr, trang_thai, ngay_tao)
VALUES
(2, 'TRAIN', 'SE3', N'Đoàn Tàu Thống Nhất SE3', N'Ga Hà Nội ➔ Ga Sài Gòn', N'Ga Hà Nội', N'Ga Sài Gòn', DATEADD(day, 10, GETDATE()), DATEADD(hour, 30, DATEADD(day, 10, GETDATE())), N'Toa 5 - Giường 09 VIP', 'SLEEPER_4', N'Nguyễn Văn Bình', '079200004567', 'ADULT', 1350000, 10000, 0, 1360000, 'ETK-TRN-776655', 'VERIFY:ETK-TRN-776655|RES-202611-0002|SE3|G09|NGUYEN VAN BINH', 'ISSUED', GETDATE());

-- 4. GIAO DỊCH THANH TOÁN (GiaoDich)
INSERT INTO dbo.GiaoDich (don_dat_cho_id, ma_giao_dich, so_tien, phuong_thuc, trang_thai, ma_tham_chieu, ghi_chu, thoi_gian_giao_dich)
VALUES
(1, 'PAY-202611-9988', 1620000, 'VNPAY', 'SUCCESS', 'VNPAY-REF-88776655', N'Thanh toán thành công qua VNPAY-QR mã đơn RES-202611-0001', GETDATE()),
(2, 'PAY-202611-9989', 1360000, 'BANK_TRANSFER', 'SUCCESS', 'MB-REF-44332211', N'Chuyển khoản VietQR thành công mã đơn RES-202611-0002', GETDATE());
GO
