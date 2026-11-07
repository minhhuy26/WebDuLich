-- ==========================================================================================
-- DỮ LIỆU MẪU BAN ĐẦU CHO HỆ THỐNG QUẢN LÝ ĐẶT VÉ DU LỊCH
-- ==========================================================================================

USE DatVeDuLich;
GO

-- 1. Thêm Người dùng nội bộ (Users)
INSERT INTO dbo.users (username, password, full_name, email, phone, role, status, created_at)
VALUES 
('admin', 'admin123', N'Quản Trị Viên Hệ Thống', 'admin@dulich.vn', '0901000001', 'ADMIN', 1, GETDATE()),
('staff_mai', 'staff123', N'Nguyễn Thị Mai (Tư vấn viên)', 'mai.nt@dulich.vn', '0902000002', 'STAFF', 1, GETDATE()),
('staff_tuan', 'staff123', N'Trần Anh Tuấn (Điều hành vé)', 'tuan.ta@dulich.vn', '0903000003', 'STAFF', 1, GETDATE());
GO

-- 2. Thêm Khách hàng mẫu (Customers)
INSERT INTO dbo.customers (full_name, phone, email, id_card, address, created_at)
VALUES
(N'Trần Văn An', '0912345678', 'an.tran@example.com', '001200001234', N'123 Cầu Giấy, Hà Nội', GETDATE()),
(N'Lê Thị Bích', '0987654321', 'bich.le@example.com', '001200005678', N'456 Lê Duẩn, Đà Nẵng', GETDATE()),
(N'Phạm Minh Cường', '0909123987', 'cuong.pm@example.com', '001200009999', N'789 Nguyễn Huệ, Quận 1, TP. Hồ Chí Minh', GETDATE());
GO

-- 3. Thêm Đơn đặt vé mẫu (Bookings)
-- Đơn 1: VÉ MÁY BAY (Hà Nội -> TP.HCM)
INSERT INTO dbo.bookings (booking_code, customer_id, ticket_type, booking_date, departure_date, return_date, departure_place, arrival_place, passenger_count, total_amount, status, special_requests, created_at)
VALUES
('BKG-VN-20261001', 1, 'FLIGHT', GETDATE(), DATEADD(day, 7, GETDATE()), NULL, N'Hà Nội (HAN)', N'TP. Hồ Chí Minh (SGN)', 1, 1650000.00, 'CONFIRMED', N'Yêu cầu ghế gần cửa sổ', GETDATE());

-- Đơn 2: VÉ TÀU HỎA (Hà Nội -> Đà Nẵng)
INSERT INTO dbo.bookings (booking_code, customer_id, ticket_type, booking_date, departure_date, return_date, departure_place, arrival_place, passenger_count, total_amount, status, special_requests, created_at)
VALUES
('BKG-SE-20261002', 2, 'TRAIN', GETDATE(), DATEADD(day, 5, GETDATE()), NULL, N'Ga Hà Nội', N'Ga Đà Nẵng', 2, 2220000.00, 'PENDING', N'2 giường nằm cạnh nhau khoang 4 VIP', GETDATE());
GO

-- 4. Thêm Chi tiết vé hành khách (TicketDetails)
-- Chi tiết cho Đơn 1 (Vé máy bay)
INSERT INTO dbo.ticket_details (booking_id, passenger_name, passenger_id_card, passenger_type, vehicle_code, seat_class, seat_number, base_price, service_fee, extra_fee, final_price, created_at)
VALUES
(1, N'Trần Văn An', '001200001234', 'ADULT', 'VN210 (Vietnam Airlines)', 'ECONOMY', '12A', 1500000.00, 150000.00, 0.00, 1650000.00, GETDATE());

-- Chi tiết cho Đơn 2 (Vé tàu hỏa - 2 hành khách)
INSERT INTO dbo.ticket_details (booking_id, passenger_name, passenger_id_card, passenger_type, vehicle_code, seat_class, seat_number, base_price, service_fee, extra_fee, final_price, created_at)
VALUES
(2, N'Lê Thị Bích', '001200005678', 'ADULT', 'Tàu SE1', 'SLEEPER_4', 'Toa 4 - Giường 11', 1100000.00, 10000.00, 0.00, 1110000.00, GETDATE()),
(2, N'Lê Hoàng Long', '001200005679', 'ADULT', 'Tàu SE1', 'SLEEPER_4', 'Toa 4 - Giường 12', 1100000.00, 10000.00, 0.00, 1110000.00, GETDATE());
GO

-- 5. Thêm Giao dịch thanh toán mẫu (Payments)
INSERT INTO dbo.payments (payment_code, booking_id, amount, payment_method, payment_date, payment_status, transaction_reference, note, created_at)
VALUES
('PAY-20261001-01', 1, 1650000.00, 'BANK_TRANSFER', GETDATE(), 'SUCCESS', 'VCB-9823482734', N'Đã thanh toán đủ qua Techcombank', GETDATE()),
('PAY-20261002-01', 2, 2220000.00, 'VN_PAY', GETDATE(), 'PENDING', 'VNPAY-PENDING-001', N'Khách đang mở link quét mã VNPAY-QR', GETDATE());
GO

-- 6. Thêm Lịch hẹn tư vấn mẫu (Consultations)
INSERT INTO dbo.consultations (consultation_code, customer_name, phone, email, ticket_type, preferred_date, status, notes, assigned_user_id, created_at)
VALUES
('ADV-20261001', N'Hoàng Thu Trang', '0978111222', 'trang.ht@gmail.com', 'FLIGHT', DATEADD(day, 2, GETDATE()), 'PENDING', N'Muốn tư vấn săn vé máy bay giá rẻ cho gia đình 4 người đi Phú Quốc tháng sau', 2, GETDATE()),
('ADV-20261002', N'Đỗ Quang Huy', '0934555666', 'huy.dq@gmail.com', 'TRAIN', DATEADD(day, 3, GETDATE()), 'CONFIRMED', N'Tư vấn hành trình trải nghiệm tàu hỏa du lịch Huế - Đà Nẵng (Đoàn kết nối di sản miền Trung)', 3, GETDATE());
GO
