USE DatVeDuLich;
GO

-- 1. CẬP NHẬT USERS
UPDATE users SET full_name = N'Quản Trị Viên Hệ Thống' WHERE username = 'admin';
UPDATE users SET full_name = N'Nguyễn Thị Mai (Nhân Viên)' WHERE username = 'nhanvien';
UPDATE users SET full_name = N'Trần Văn An (Khách Hàng)' WHERE username = 'khachhang';
UPDATE users SET full_name = N'Nguyễn Thị Mai (Tư vấn viên)' WHERE username = 'staff_mai';
UPDATE users SET full_name = N'Trần Anh Tuấn (Điều hành vé)' WHERE username = 'staff_tuan';

-- 2. CẬP NHẬT CUSTOMERS
UPDATE customers SET full_name = N'Trần Văn An', address = N'123 Cầu Giấy, Hà Nội' WHERE email = 'an.tran@example.com';
UPDATE customers SET full_name = N'Lê Thị Bích', address = N'456 Lê Duẩn, Đà Nẵng' WHERE email = 'bich.le@example.com';
UPDATE customers SET full_name = N'Phạm Minh Cường', address = N'789 Nguyễn Huệ, Quận 1, TP. Hồ Chí Minh' WHERE email = 'cuong.pm@example.com';

-- 3. CẬP NHẬT BOOKINGS
UPDATE bookings SET departure_place = N'Hà Nội (HAN)', arrival_place = N'TP. Hồ Chí Minh (SGN)', special_requests = N'Yêu cầu ghế gần cửa sổ' WHERE booking_code = 'BKG-VN-20261001';
UPDATE bookings SET departure_place = N'Ga Hà Nội', arrival_place = N'Ga Đà Nẵng', special_requests = N'2 giường nằm cạnh nhau khoang 4 VIP' WHERE booking_code = 'BKG-SE-20261002';

-- 4. CẬP NHẬT TICKET_DETAILS
UPDATE ticket_details SET passenger_name = N'Trần Văn An', vehicle_code = N'VN210 (Vietnam Airlines)' WHERE booking_id = 1;
UPDATE ticket_details SET passenger_name = N'Lê Thị Bích', vehicle_code = N'Tàu SE1', seat_number = N'Toa 3 - Giường 11' WHERE passenger_id_card = '001200005678';
UPDATE ticket_details SET passenger_name = N'Lê Hoàng Long', vehicle_code = N'Tàu SE1', seat_number = N'Toa 3 - Giường 12' WHERE passenger_id_card = '001200005679';

-- 5. CẬP NHẬT PAYMENTS
UPDATE payments SET note = N'Đã thanh toán đủ qua ngân hàng trực tuyến' WHERE payment_code = 'PAY-20261001-01';
UPDATE payments SET note = N'Khách đang mở link quét mã VNPAY-QR' WHERE payment_code = 'PAY-20261002-01';

-- 6. CẬP NHẬT CONSULTATIONS
UPDATE consultations SET customer_name = N'Hoàng Thu Trang', notes = N'Muốn tư vấn săn vé máy bay giá rẻ cho gia đình 4 người đi Phú Quốc tháng sau' WHERE consultation_code = 'ADV-20261001';
UPDATE consultations SET customer_name = N'Đỗ Quang Huy', notes = N'Tư vấn hành trình trải nghiệm tàu hỏa du lịch Huế - Đà Nẵng (Đoàn kết nối di sản miền Trung)' WHERE consultation_code = 'ADV-20261002';

-- 7. CẬP NHẬT TRAIN_STATIONS
UPDATE train_stations SET station_name = N'Ga Hà Nội', route_name = N'Hà Nội - Sài Gòn' WHERE station_code = 'HAN';
UPDATE train_stations SET station_name = N'Ga Nam Định', route_name = N'Hà Nội - Sài Gòn' WHERE station_code = 'NDI';
UPDATE train_stations SET station_name = N'Ga Ninh Bình', route_name = N'Hà Nội - Sài Gòn' WHERE station_code = 'NBI';
UPDATE train_stations SET station_name = N'Ga Thanh Hóa', route_name = N'Hà Nội - Sài Gòn' WHERE station_code = 'THO';
UPDATE train_stations SET station_name = N'Ga Vinh', route_name = N'Hà Nội - Sài Gòn' WHERE station_code = 'VIN';
UPDATE train_stations SET station_name = N'Ga Đồng Hới', route_name = N'Hà Nội - Sài Gòn' WHERE station_code = 'DHO';
UPDATE train_stations SET station_name = N'Ga Huế', route_name = N'Hà Nội - Sài Gòn' WHERE station_code = 'HUE';
UPDATE train_stations SET station_name = N'Ga Đà Nẵng', route_name = N'Hà Nội - Sài Gòn' WHERE station_code = 'DAN';
UPDATE train_stations SET station_name = N'Ga Nha Trang', route_name = N'Hà Nội - Sài Gòn' WHERE station_code = 'NTR';
UPDATE train_stations SET station_name = N'Ga Bình Thuận', route_name = N'Hà Nội - Sài Gòn' WHERE station_code = 'BTH';
UPDATE train_stations SET station_name = N'Ga Sài Gòn', route_name = N'Hà Nội - Sài Gòn' WHERE station_code = 'SGO';

-- 8. CẬP NHẬT TRAINS
UPDATE trains SET departure_station_name = N'Ga Hà Nội', arrival_station_name = N'Ga Sài Gòn', route_name = N'Hà Nội - Sài Gòn' WHERE train_code IN ('SE1', 'SE3', 'SE5');

-- 9. CẬP NHẬT TRAIN_COACHES
UPDATE train_coaches SET coach_name = N'Toa 1 - Ghế Mềm Điều Hòa' WHERE coach_number = 1;
UPDATE train_coaches SET coach_name = N'Toa 2 - Giường Nằm Khoang 6' WHERE coach_number = 2;
UPDATE train_coaches SET coach_name = N'Toa 3 - Giường Nằm Khoang 4 VIP' WHERE coach_number = 3;

-- 10. CẬP NHẬT FLIGHTS
UPDATE flights SET departure_city = N'Hà Nội', arrival_city = N'TP. Hồ Chí Minh' WHERE departure_airport = 'HAN' AND arrival_airport = 'SGN';
UPDATE flights SET departure_city = N'Hà Nội', arrival_city = N'Đà Nẵng' WHERE departure_airport = 'HAN' AND arrival_airport = 'DAD';
UPDATE flights SET departure_city = N'TP. Hồ Chí Minh', arrival_city = N'Phú Quốc' WHERE departure_airport = 'SGN' AND arrival_airport = 'PQC';
UPDATE flights SET airline = N'Vietnam Airlines' WHERE airline LIKE '%Vietnam%';
UPDATE flights SET airline = N'Vietjet Air' WHERE airline LIKE '%Vietjet%';
UPDATE flights SET airline = N'Bamboo Airways' WHERE airline LIKE '%Bamboo%';
GO
