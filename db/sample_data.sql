-- Tập lệnh Batch SQL tạo dữ liệu mẫu
-- ==========================
-- PHẦN 1: KHAI BÁO BIẾN ĐẦU VÀO
-- ==========================
SET @number_user = 100;            -- Số lượng user
SET @number_driver = 50;          -- Số lượng driver
SET @number_ride = 100;           -- Số lượng ride
SET @number_payment = 80;         -- Số lượng giao dịch thanh toán
SET @ride_date = CURDATE();       -- Ngày phát sinh ride (nếu cần thay đổi thì set DATE cụ thể)
SET @car_type_main = '4S';        -- Loại xe mặc định
SET @car_type_alt = '7S';         -- Loại xe khác
SET @user_id_min = 1;             -- Khoảng ID người dùng >> mở bảng để lấy min/max
SET @user_id_max = 100;
SET @driver_id_min = 1; 		  -- Khoảng ID tài xế  >> mở bảng để lấy min/max
SET @driver_id_max = 50;

-- ==========================
-- PHẦN 2: GỌI THỦ TỤC TẠO DỮ LIỆU GIẢ LẬP
-- 			CÓ THỂ GỌI NHIỀU LẦN
-- ==========================

-- Tạo users và rides ngẫu nhiên
CALL sp_insert_bulk_users_drivers_rides(
    @number_user,
    @number_driver,
    @number_ride,
    @ride_date,
    @car_type_main
);

-- Sinh giao dịch thanh toán cho các rides
CALL sp_insert_random_payments(
    @number_payment,
    @ride_date
);

-- Sinh thêm ride khác với phân vùng user-driver cụ thể
CALL sp_insert_random_k_rides(
    @ride_date,
    20,                  -- số ride bổ sung
    @car_type_alt,
    @user_id_min,
    @user_id_max,
    @driver_id_min,
    @driver_id_max
);

-- Thêm số lượng tham chiếu refferal
call sp_insert_random_referrals(@refferal_num);
