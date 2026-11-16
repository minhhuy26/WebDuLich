-- ==========================================================================================
-- HỆ THỐNG QUẢN LÝ ĐẶT VÉ DU LỊCH (TRAVEL TICKET BOOKING & MANAGEMENT PORTAL)
-- DỮ LIỆU MẪU CSDL SQL SERVER (5 BẢNG CỐT LÕI - 2 LOẠI VÉ: MÁY BAY & TÀU HỎA)
-- ==========================================================================================

USE DatVeDuLich;
GO

-- 1. TÀI KHOẢN NGƯỜI DÙNG (4 VAI TRÒ CHUẨN RBAC THEO TÀI LIỆU CỦA THẦY)
INSERT INTO dbo.users (username, password, full_name, email, phone, role, status, created_at)
VALUES
('admin', '$2a$10$6cmGlTKrQWyzEGgNky/4VeTzJwXNapAHF4zUgYVlrW4XgUiSsutRy', N'Quản Trị Viên Hệ Thống', 'admin@dulich.vn', '0901000001', 'ROLE_ADMIN', 1, GETDATE()),
('operator_mai', '$2a$10$UZHylGdYv7oQf0XvjHV/8eZxQ8UZPU4AwgOTmND0TCm72kNq0Eu42', N'Nguyễn Thị Mai (Điều hành vé)', 'mai.operator@dulich.vn', '0902000002', 'ROLE_OPERATOR', 1, GETDATE()),
('agent_saigontour', '$2a$10$Xe0a/RzLD.Lujld9jwFYOeZLV4H6pX9pP42sV/.WYt4ykc.XXBzz.', N'Đại Lý Du Lịch Sài Gòn', 'contact@saigontour.vn', '0903000003', 'ROLE_AGENT', 1, GETDATE()),
('traveler_an', '$2a$10$13iuAPqzfVKeyXbSlJgewOMkUO21XbxDtRp1u2SX7zFeC9tJEcXhm', N'Trần Văn An (Du khách)', 'an.tran@gmail.com', '0912345678', 'ROLE_TRAVELER', 1, GETDATE());
GO

-- 2. LỊCH HẸN TƯ VẤN (APPOINTMENTS - WORKFLOW 4 CỦA THẦY)
INSERT INTO dbo.appointments (appointment_code, user_id, customer_name, phone, email, preferred_date, transport_type, notes, status, assigned_operator_id, created_at)
VALUES
('APT-202611-001', 4, N'Trần Văn An', '0912345678', 'an.tran@gmail.com', DATEADD(day, 2, GETDATE()), 'AIR', N'Tư vấn đặt vé đoàn bay Phú Quốc 15 người dịp Tết', 'CONFIRMED', 2, GETDATE()),
('APT-202611-002', 3, N'Đại Lý Du Lịch Sài Gòn', '0903000003', 'contact@saigontour.vn', DATEADD(day, 3, GETDATE()), 'TRAIN', N'Đăng ký giữ chỗ nguyên toa giường nằm tàu SE1 tuyến Bắc Nam', 'PENDING', 2, GETDATE()),
('APT-202611-003', NULL, N'Lê Hoàng Long', '0988776655', 'long.le@gmail.com', DATEADD(day, 1, GETDATE()), 'AIR', N'Tư vấn gói bảo hiểm và đổi chuyến bay Hà Nội - Đà Nẵng', 'PENDING', NULL, GETDATE());
GO

-- 3. KHO VÉ & LỊCH TRÌNH 2 LOẠI PHƯƠNG TIỆN (TICKETS - INVENTORY)
-- 3.1. VÉ MÁY BAY (AIR) - Vietnam Airlines, Vietjet Air, Bamboo Airways
INSERT INTO dbo.tickets (transport_type, vehicle_code, vehicle_name, route_info, departure_place, arrival_place, departure_time, arrival_time, seat_number, seat_class, passenger_name, id_card, passenger_type, base_price, service_fee, extra_fee, final_price, ticket_code, qr_code_data, status, created_at)
VALUES
('AIR', 'VN210', N'Vietnam Airlines Airbus A321', N'Hà Nội (HAN) ➔ TP. Hồ Chí Minh (SGN)', N'Hà Nội (HAN)', N'TP. Hồ Chí Minh (SGN)', DATEADD(day, 3, GETDATE()), DATEADD(minute, 130, DATEADD(day, 3, GETDATE())), '12A', 'ECONOMY', NULL, NULL, 'ADULT', 1500000, 120000, 0, 1620000, NULL, NULL, 'AVAILABLE', GETDATE()),
('AIR', 'VN210', N'Vietnam Airlines Airbus A321', N'Hà Nội (HAN) ➔ TP. Hồ Chí Minh (SGN)', N'Hà Nội (HAN)', N'TP. Hồ Chí Minh (SGN)', DATEADD(day, 3, GETDATE()), DATEADD(minute, 130, DATEADD(day, 3, GETDATE())), '12B', 'ECONOMY', NULL, NULL, 'ADULT', 1500000, 120000, 0, 1620000, NULL, NULL, 'AVAILABLE', GETDATE()),
('AIR', 'VN210', N'Vietnam Airlines Airbus A321', N'Hà Nội (HAN) ➔ TP. Hồ Chí Minh (SGN)', N'Hà Nội (HAN)', N'TP. Hồ Chí Minh (SGN)', DATEADD(day, 3, GETDATE()), DATEADD(minute, 130, DATEADD(day, 3, GETDATE())), '02A', 'BUSINESS', NULL, NULL, 'ADULT', 3200000, 120000, 0, 3320000, NULL, NULL, 'AVAILABLE', GETDATE()),
('AIR', 'VJ134', N'Vietjet Air Airbus A320', N'Hà Nội (HAN) ➔ Đà Nẵng (DAD)', N'Hà Nội (HAN)', N'Đà Nẵng (DAD)', DATEADD(day, 2, GETDATE()), DATEADD(minute, 80, DATEADD(day, 2, GETDATE())), '15C', 'ECONOMY', NULL, NULL, 'ADULT', 980000, 120000, 0, 1100000, NULL, NULL, 'AVAILABLE', GETDATE()),
('AIR', 'QH203', N'Bamboo Airways Embraer 190', N'Hà Nội (HAN) ➔ Phú Quốc (PQC)', N'Hà Nội (HAN)', N'Phú Quốc (PQC)', DATEADD(day, 5, GETDATE()), DATEADD(minute, 135, DATEADD(day, 5, GETDATE())), '08A', 'ECONOMY', NULL, NULL, 'ADULT', 1850000, 120000, 0, 1970000, NULL, NULL, 'AVAILABLE', GETDATE()),
('AIR', 'VN211', N'Vietnam Airlines Airbus A321', N'Sài Gòn (SGN) ➔ Hà Nội (HAN)', N'Sài Gòn (SGN)', N'Hà Nội (HAN)', DATEADD(day, 2, GETDATE()), DATEADD(minute, 130, DATEADD(day, 2, GETDATE())), '16A', 'ECONOMY', NULL, NULL, 'ADULT', 1550000, 120000, 0, 1670000, NULL, NULL, 'AVAILABLE', GETDATE()),
('AIR', 'VJ135', N'Vietjet Air Airbus A320', N'Đà Nẵng (DAD) ➔ Hà Nội (HAN)', N'Đà Nẵng (DAD)', N'Hà Nội (HAN)', DATEADD(day, 3, GETDATE()), DATEADD(minute, 80, DATEADD(day, 3, GETDATE())), '11B', 'ECONOMY', NULL, NULL, 'ADULT', 950000, 120000, 0, 1070000, NULL, NULL, 'AVAILABLE', GETDATE()),
('AIR', 'QH204', N'Bamboo Airways Embraer 190', N'Phú Quốc (PQC) ➔ Hà Nội (HAN)', N'Phú Quốc (PQC)', N'Hà Nội (HAN)', DATEADD(day, 4, GETDATE()), DATEADD(minute, 135, DATEADD(day, 4, GETDATE())), '07C', 'ECONOMY', NULL, NULL, 'ADULT', 1890000, 120000, 0, 2010000, NULL, NULL, 'AVAILABLE', GETDATE());

-- 3.2. VÉ TÀU HỎA (TRAIN) - Tuyến Thống Nhất Bắc - Nam (SE1, SE2, SE3, SE4, SE5)
INSERT INTO dbo.tickets (transport_type, vehicle_code, vehicle_name, route_info, departure_place, arrival_place, departure_time, arrival_time, seat_number, seat_class, passenger_name, id_card, passenger_type, base_price, service_fee, extra_fee, final_price, ticket_code, qr_code_data, status, created_at)
VALUES
('TRAIN', 'SE1', N'Đoàn Tàu Thống Nhất SE1', N'Ga Hà Nội ➔ Ga Đà Nẵng', N'Ga Hà Nội', N'Ga Đà Nẵng', DATEADD(day, 2, GETDATE()), DATEADD(hour, 16, DATEADD(day, 2, GETDATE())), N'Toa 2 - Ghế 15', 'SOFT_SEAT', NULL, NULL, 'ADULT', 580000, 10000, 0, 590000, NULL, NULL, 'AVAILABLE', GETDATE()),
('TRAIN', 'SE1', N'Đoàn Tàu Thống Nhất SE1', N'Ga Hà Nội ➔ Ga Đà Nẵng', N'Ga Hà Nội', N'Ga Đà Nẵng', DATEADD(day, 2, GETDATE()), DATEADD(hour, 16, DATEADD(day, 2, GETDATE())), N'Toa 4 - Giường 11', 'SLEEPER_4', NULL, NULL, 'ADULT', 950000, 10000, 0, 960000, NULL, NULL, 'AVAILABLE', GETDATE()),
('TRAIN', 'SE3', N'Đoàn Tàu Thống Nhất SE3', N'Ga Hà Nội ➔ Ga Sài Gòn', N'Ga Hà Nội', N'Ga Sài Gòn', DATEADD(day, 4, GETDATE()), DATEADD(hour, 30, DATEADD(day, 4, GETDATE())), N'Toa 5 - Giường 08 VIP', 'SLEEPER_4', NULL, NULL, 'ADULT', 1350000, 10000, 0, 1360000, NULL, NULL, 'AVAILABLE', GETDATE()),
('TRAIN', 'SE5', N'Đoàn Tàu Thống Nhất SE5', N'Ga Hà Nội ➔ Ga Huế', N'Ga Hà Nội', N'Ga Huế', DATEADD(day, 3, GETDATE()), DATEADD(hour, 13, DATEADD(day, 3, GETDATE())), N'Toa 3 - Ghế 22', 'SOFT_SEAT', NULL, NULL, 'ADULT', 490000, 10000, 0, 500000, NULL, NULL, 'AVAILABLE', GETDATE()),
('TRAIN', 'SE2', N'Đoàn Tàu Thống Nhất SE2', N'Ga Sài Gòn ➔ Ga Hà Nội', N'Ga Sài Gòn', N'Ga Hà Nội', DATEADD(day, 2, GETDATE()), DATEADD(hour, 30, DATEADD(day, 2, GETDATE())), N'Toa 3 - Giường 09 VIP', 'SLEEPER_4', NULL, NULL, 'ADULT', 1350000, 10000, 0, 1360000, NULL, NULL, 'AVAILABLE', GETDATE()),
('TRAIN', 'SE4', N'Đoàn Tàu Thống Nhất SE4', N'Ga Đà Nẵng ➔ Ga Hà Nội', N'Ga Đà Nẵng', N'Ga Hà Nội', DATEADD(day, 3, GETDATE()), DATEADD(hour, 16, DATEADD(day, 3, GETDATE())), N'Toa 2 - Ghế 18', 'SOFT_SEAT', NULL, NULL, 'ADULT', 580000, 10000, 0, 590000, NULL, NULL, 'AVAILABLE', GETDATE());
GO

-- 4. ĐƠN ĐẶT CHỖ MẪU ĐÃ XÁC NHẬN & PHÁT HÀNH VÉ (RESERVATIONS & TRANS_DETAILS)
INSERT INTO dbo.reservations (booking_code, user_id, customer_name, customer_phone, customer_email, transport_type, departure_place, arrival_place, departure_date, return_date, is_round_trip, passenger_count, total_amount, status, hold_expires_at, cancel_reason, created_at)
VALUES
('RES-202611-0001', 4, N'Trần Văn An', '0912345678', 'an.tran@gmail.com', 'AIR', N'Hà Nội (HAN)', N'TP. Hồ Chí Minh (SGN)', DATEADD(day, 7, GETDATE()), NULL, 0, 1, 1620000, 'CONFIRMED', DATEADD(hour, 1, GETDATE()), NULL, GETDATE());

-- Cập nhật 1 vé thành ĐÃ XUẤT VÉ gán vào đơn RES-202611-0001
INSERT INTO dbo.tickets (reservation_id, transport_type, vehicle_code, vehicle_name, route_info, departure_place, arrival_place, departure_time, arrival_time, seat_number, seat_class, passenger_name, id_card, passenger_type, base_price, service_fee, extra_fee, final_price, ticket_code, qr_code_data, status, created_at)
VALUES
(1, 'AIR', 'VN210', N'Vietnam Airlines Airbus A321', N'Hà Nội (HAN) ➔ TP. Hồ Chí Minh (SGN)', N'Hà Nội (HAN)', N'TP. Hồ Chí Minh (SGN)', DATEADD(day, 7, GETDATE()), DATEADD(minute, 130, DATEADD(day, 7, GETDATE())), '14C', 'ECONOMY', N'Trần Văn An', '001200001234', 'ADULT', 1500000, 120000, 0, 1620000, 'ETK-AIR-998822', 'VERIFY:ETK-AIR-998822|RES-202611-0001|VN210|14C|TRAN VAN AN', 'ISSUED', GETDATE());

-- Ghi nhận lịch sử giao dịch thanh toán trong trans_details
INSERT INTO dbo.trans_details (reservation_id, payment_code, amount, payment_method, payment_status, transaction_reference, note, transaction_time)
VALUES
(1, 'PAY-202611-9988', 1620000, 'VNPAY', 'SUCCESS', 'VNPAY-REF-88776655', N'Thanh toán thành công qua VNPAY-QR mã đơn RES-202611-0001', GETDATE());
GO
