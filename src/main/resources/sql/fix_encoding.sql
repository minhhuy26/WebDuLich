USE DatVeDuLich;
GO

-- 1. Sửa tên ga tàu
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
GO

-- 2. Sửa thông tin tàu
UPDATE trains SET departure_station_name = N'Ga Hà Nội', arrival_station_name = N'Ga Sài Gòn', route_name = N'Hà Nội - Sài Gòn' WHERE train_code IN ('SE1', 'SE3', 'SE5');
GO

-- 3. Sửa thông tin toa tàu
UPDATE train_coaches SET coach_name = N'Toa 1 - Ghế Mềm Điều Hòa' WHERE coach_number = 1;
UPDATE train_coaches SET coach_name = N'Toa 2 - Giường Nằm Khoang 6' WHERE coach_number = 2;
UPDATE train_coaches SET coach_name = N'Toa 3 - Giường Nằm Khoang 4 VIP' WHERE coach_number = 3;
GO

-- 4. Sửa thông tin chuyến bay
UPDATE flights SET departure_city = N'Hà Nội', arrival_city = N'TP. Hồ Chí Minh' WHERE departure_airport = 'HAN' AND arrival_airport = 'SGN';
UPDATE flights SET departure_city = N'Hà Nội', arrival_city = N'Đà Nẵng' WHERE departure_airport = 'HAN' AND arrival_airport = 'DAD';
UPDATE flights SET departure_city = N'TP. Hồ Chí Minh', arrival_city = N'Phú Quốc' WHERE departure_airport = 'SGN' AND arrival_airport = 'PQC';
UPDATE flights SET airline = N'Vietnam Airlines' WHERE airline LIKE '%Vietnam%';
UPDATE flights SET airline = N'Vietjet Air' WHERE airline LIKE '%Vietjet%';
UPDATE flights SET airline = N'Bamboo Airways' WHERE airline LIKE '%Bamboo%';
GO

-- 5. Sửa thông tin người dùng
UPDATE users SET full_name = N'Quản Trị Viên Hệ Thống' WHERE username = 'admin';
UPDATE users SET full_name = N'Nguyễn Thị Mai (Nhân Viên)' WHERE username = 'nhanvien';
UPDATE users SET full_name = N'Trần Văn An (Khách Hàng)' WHERE username = 'khachhang';
IF EXISTS (SELECT 1 FROM users WHERE username = 'staff_mai')
    UPDATE users SET full_name = N'Nguyễn Thị Mai (Tư vấn viên)' WHERE username = 'staff_mai';
IF EXISTS (SELECT 1 FROM users WHERE username = 'staff_tuan')
    UPDATE users SET full_name = N'Trần Anh Tuấn (Điều hành vé)' WHERE username = 'staff_tuan';
GO

-- 6. Sửa chỗ ngồi & vị trí vé tàu (train_seats)
UPDATE train_seats SET seat_label = N'Ghế 01', position_note = N'Cửa sổ' WHERE coach_id = 1 AND seat_number = 1;
UPDATE train_seats SET seat_label = N'Ghế 02', position_note = N'Lối đi' WHERE coach_id = 1 AND seat_number = 2;
UPDATE train_seats SET seat_label = N'Ghế 03', position_note = N'Lối đi' WHERE coach_id = 1 AND seat_number = 3;
UPDATE train_seats SET seat_label = N'Ghế 04', position_note = N'Cửa sổ' WHERE coach_id = 1 AND seat_number = 4;
UPDATE train_seats SET seat_label = N'Ghế 05', position_note = N'Cửa sổ' WHERE coach_id = 1 AND seat_number = 5;
UPDATE train_seats SET seat_label = N'Ghế 06', position_note = N'Lối đi' WHERE coach_id = 1 AND seat_number = 6;
UPDATE train_seats SET seat_label = N'Ghế 07', position_note = N'Lối đi' WHERE coach_id = 1 AND seat_number = 7;
UPDATE train_seats SET seat_label = N'Ghế 08', position_note = N'Cửa sổ' WHERE coach_id = 1 AND seat_number = 8;
UPDATE train_seats SET seat_label = N'Ghế 09', position_note = N'Cửa sổ' WHERE coach_id = 1 AND seat_number = 9;
UPDATE train_seats SET seat_label = N'Ghế 10', position_note = N'Lối đi' WHERE coach_id = 1 AND seat_number = 10;
UPDATE train_seats SET seat_label = N'Ghế 11', position_note = N'Lối đi' WHERE coach_id = 1 AND seat_number = 11;
UPDATE train_seats SET seat_label = N'Ghế 12', position_note = N'Cửa sổ' WHERE coach_id = 1 AND seat_number = 12;
UPDATE train_seats SET seat_label = N'Ghế 13', position_note = N'Cửa sổ' WHERE coach_id = 1 AND seat_number = 13;
UPDATE train_seats SET seat_label = N'Ghế 14', position_note = N'Lối đi' WHERE coach_id = 1 AND seat_number = 14;
UPDATE train_seats SET seat_label = N'Ghế 15', position_note = N'Lối đi' WHERE coach_id = 1 AND seat_number = 15;
UPDATE train_seats SET seat_label = N'Ghế 16', position_note = N'Cửa sổ' WHERE coach_id = 1 AND seat_number = 16;

UPDATE train_seats SET seat_label = N'Khoang 1 - Giường 01 (T1)', position_note = N'Tầng 1 - Thấp' WHERE coach_id = 2 AND seat_number = 1;
UPDATE train_seats SET seat_label = N'Khoang 1 - Giường 02 (T1)', position_note = N'Tầng 1 - Thấp' WHERE coach_id = 2 AND seat_number = 2;
UPDATE train_seats SET seat_label = N'Khoang 1 - Giường 03 (T2)', position_note = N'Tầng 2 - Giữa' WHERE coach_id = 2 AND seat_number = 3;
UPDATE train_seats SET seat_label = N'Khoang 1 - Giường 04 (T2)', position_note = N'Tầng 2 - Giữa' WHERE coach_id = 2 AND seat_number = 4;
UPDATE train_seats SET seat_label = N'Khoang 1 - Giường 05 (T3)', position_note = N'Tầng 3 - Cao' WHERE coach_id = 2 AND seat_number = 5;
UPDATE train_seats SET seat_label = N'Khoang 1 - Giường 06 (T3)', position_note = N'Tầng 3 - Cao' WHERE coach_id = 2 AND seat_number = 6;
UPDATE train_seats SET seat_label = N'Khoang 2 - Giường 07 (T1)', position_note = N'Tầng 1 - Thấp' WHERE coach_id = 2 AND seat_number = 7;
UPDATE train_seats SET seat_label = N'Khoang 2 - Giường 08 (T1)', position_note = N'Tầng 1 - Thấp' WHERE coach_id = 2 AND seat_number = 8;
UPDATE train_seats SET seat_label = N'Khoang 2 - Giường 09 (T2)', position_note = N'Tầng 2 - Giữa' WHERE coach_id = 2 AND seat_number = 9;
UPDATE train_seats SET seat_label = N'Khoang 2 - Giường 10 (T2)', position_note = N'Tầng 2 - Giữa' WHERE coach_id = 2 AND seat_number = 10;
UPDATE train_seats SET seat_label = N'Khoang 2 - Giường 11 (T3)', position_note = N'Tầng 3 - Cao' WHERE coach_id = 2 AND seat_number = 11;
UPDATE train_seats SET seat_label = N'Khoang 2 - Giường 12 (T3)', position_note = N'Tầng 3 - Cao' WHERE coach_id = 2 AND seat_number = 12;

UPDATE train_seats SET seat_label = N'Khoang 1 - Giường 01 (T1)', position_note = N'Tầng 1 VIP' WHERE coach_id = 3 AND seat_number = 1;
UPDATE train_seats SET seat_label = N'Khoang 1 - Giường 02 (T1)', position_note = N'Tầng 1 VIP' WHERE coach_id = 3 AND seat_number = 2;
UPDATE train_seats SET seat_label = N'Khoang 1 - Giường 03 (T2)', position_note = N'Tầng 2 VIP' WHERE coach_id = 3 AND seat_number = 3;
UPDATE train_seats SET seat_label = N'Khoang 1 - Giường 04 (T2)', position_note = N'Tầng 2 VIP' WHERE coach_id = 3 AND seat_number = 4;
UPDATE train_seats SET seat_label = N'Khoang 2 - Giường 05 (T1)', position_note = N'Tầng 1 VIP' WHERE coach_id = 3 AND seat_number = 5;
UPDATE train_seats SET seat_label = N'Khoang 2 - Giường 06 (T1)', position_note = N'Tầng 1 VIP' WHERE coach_id = 3 AND seat_number = 6;
UPDATE train_seats SET seat_label = N'Khoang 2 - Giường 07 (T2)', position_note = N'Tầng 2 VIP' WHERE coach_id = 3 AND seat_number = 7;
UPDATE train_seats SET seat_label = N'Khoang 2 - Giường 08 (T2)', position_note = N'Tầng 2 VIP' WHERE coach_id = 3 AND seat_number = 8;

UPDATE train_seats SET seat_label = N'Ghế 01', position_note = N'Cửa sổ' WHERE coach_id = 4 AND seat_number = 1;
UPDATE train_seats SET seat_label = N'Ghế 02', position_note = N'Lối đi' WHERE coach_id = 4 AND seat_number = 2;
UPDATE train_seats SET seat_label = N'Ghế 03', position_note = N'Lối đi' WHERE coach_id = 4 AND seat_number = 3;
UPDATE train_seats SET seat_label = N'Ghế 04', position_note = N'Cửa sổ' WHERE coach_id = 4 AND seat_number = 4;
UPDATE train_seats SET seat_label = N'Ghế 05', position_note = N'Cửa sổ' WHERE coach_id = 4 AND seat_number = 5;
UPDATE train_seats SET seat_label = N'Ghế 06', position_note = N'Lối đi' WHERE coach_id = 4 AND seat_number = 6;
UPDATE train_seats SET seat_label = N'Ghế 07', position_note = N'Lối đi' WHERE coach_id = 4 AND seat_number = 7;
UPDATE train_seats SET seat_label = N'Ghế 08', position_note = N'Cửa sổ' WHERE coach_id = 4 AND seat_number = 8;
GO
