-- Tạo cơ sở dữ liệu mileage_test >> đổi thành tên khác
CREATE DATABASE IF NOT EXISTS mileage_test CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE mileage_test;

-- bảng tài xế
CREATE TABLE k_drivers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL DEFAULT '',
    phone VARCHAR(20),
    email VARCHAR(100),
    license_number VARCHAR(50),
    car_plate VARCHAR(20),
    car_type VARCHAR(20),
    rating DECIMAL(3,2) DEFAULT 5.00,
    joined_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    status ENUM('active', 'inactive', 'banned') DEFAULT 'active'
);

-- Bảng người dùng
CREATE TABLE `k_users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `phone` varchar(15) COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `password_hash` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `referral_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `referrer_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `phone` (`phone`),
  UNIQUE KEY `referral_code` (`referral_code`),
  KEY `idx_k_users_phone` (`phone`)
) ENGINE=InnoDB AUTO_INCREMENT=111 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng chuyến đi
CREATE TABLE `k_rides` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `driver_id` bigint NOT NULL,
  `car_type` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `start_location` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `end_location` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `fare` decimal(10,2) NOT NULL,
  `status` enum('requested','confirmed','completed','cancelled') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'requested',
  `requested_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `confirmed_at` datetime DEFAULT NULL,
  `completed_at` datetime DEFAULT NULL,
  `cancelled_at` datetime DEFAULT NULL,
  `reward_points_used` int DEFAULT '0',
  `distance_km` double unsigned NOT NULL,
  `ride_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_k_rides_user_id` (`user_id`),
  KEY `idx_k_rides_driver_id` (`driver_id`),
  KEY `idx_k_rides_status` (`status`),
  CONSTRAINT `k_rides_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `k_users` (`id`),
  CONSTRAINT `k_rides_ibfk_2` FOREIGN KEY (`driver_id`) REFERENCES `k_users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4077 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;



-- bảng lịch sử lưu thông tin thanh toán của khách hàng 
CREATE TABLE `k_payments` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `ride_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `amount` decimal(10,2) NOT NULL,
  `payment_method` enum('cash','card','momo','zalopay') COLLATE utf8mb4_unicode_ci NOT NULL,
  `paid_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `status` enum('pending','success','failed') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'pending',
  `transaction_ref` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `ride_id` (`ride_id`),
  KEY `idx_k_payments_user_id` (`user_id`),
  KEY `idx_k_payments_status` (`status`),
  CONSTRAINT `k_payments_ibfk_1` FOREIGN KEY (`ride_id`) REFERENCES `k_rides` (`id`),
  CONSTRAINT `k_payments_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `k_users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1607 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng thưởng
CREATE TABLE `k_reward_points` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `points` int NOT NULL,
  `points_type` enum('earn','redeem') COLLATE utf8mb4_unicode_ci NOT NULL,
  `ride_id` bigint DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  description VARCHAR(255),
  KEY `ride_id` (`ride_id`),
  KEY `idx_k_reward_points_user_id` (`user_id`),
  CONSTRAINT `k_reward_points_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `k_users` (`id`),
  CONSTRAINT `k_reward_points_ibfk_2` FOREIGN KEY (`ride_id`) REFERENCES `k_rides` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=46 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- bảng tham chiếu cho người giới thiệu 
CREATE TABLE `k_referrals` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `referrer_id` bigint NOT NULL,
  `referee_id` bigint NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_k_referrer_referee` (`referrer_id`,`referee_id`),
  KEY `idx_k_referrals_referrer_id` (`referrer_id`),
  KEY `idx_k_referrals_referee_id` (`referee_id`),
  CONSTRAINT `k_referrals_ibfk_1` FOREIGN KEY (`referrer_id`) REFERENCES `k_users` (`id`),
  CONSTRAINT `k_referrals_ibfk_2` FOREIGN KEY (`referee_id`) REFERENCES `k_users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=51 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- View tổng điểm và quãng đường theo user
CREATE OR REPLACE VIEW v_user_rewards_summary AS
SELECT
    u.name AS name,
    u.phone AS phone,
    u.email AS email,
    u.referral_code AS referral_code,
    r.car_type AS car_type,
    r.start_location AS start_location,
    r.end_location AS end_location,
    r.completed_at AS completed_at,
    r.requested_at AS requested_at,
    rp.points AS points,
    rp.points_type AS points_type,
    rp.created_at AS created_at
FROM
    k_users u
LEFT JOIN
    k_rides r ON u.id = r.user_id
LEFT JOIN
    k_reward_points rp ON r.id = rp.ride_id AND u.id = rp.user_id
LEFT JOIN
    k_payments p ON r.id = p.ride_id AND u.id = p.user_id;

-- Function: chuyển đổi khoảng cách sang điểm
Drop PROCEDURE if exists calc_reward_points;
CREATE FUNCTION calc_reward_points(distance_km DOUBLE)
RETURNS INT
DETERMINISTIC
BEGIN
    RETURN FLOOR(distance_km * 10); -- 1 km = 10 điểm
END;

-- sinh cặp dữ liệu khách hàng và chuyến đi(khác với sinh dữ liệu theo bảng đơn)
drop procedure if exists sp_insert_bulk_users_drivers_rides;
CREATE PROCEDURE sp_insert_bulk_users_drivers_rides(
    IN in_user_count INT,
    IN in_driver_count INT,
    IN in_ride_count INT,
    IN in_ride_date DATE,
    IN in_car_type VARCHAR(20)
)
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE user_min BIGINT;
    DECLARE user_max BIGINT;
    DECLARE driver_min BIGINT;
    DECLARE driver_max BIGINT;
    DECLARE rand_user BIGINT;
    DECLARE rand_driver BIGINT;
    DECLARE ride_status ENUM('requested','confirmed','completed','cancelled');
    DECLARE rand_fare DECIMAL(10,2);
    DECLARE rand_distance DOUBLE;
    DECLARE reward_used INT;
    DECLARE base_time DATETIME;
    DECLARE start_loc VARCHAR(255);
    DECLARE end_loc VARCHAR(255);
    DECLARE new_user_id BIGINT;

    -- 1. Tạo users
    SET i = 0;
    WHILE i < in_user_count DO
        INSERT INTO k_users (name, phone, email, password_hash, referral_code)
        VALUES (
            CONCAT('User_', i),
            CONCAT('090', LPAD(FLOOR(RAND() * 10000000), 7, '0')),
            CONCAT('user', i, '@mail.com'),
            MD5(RAND()),
            CONCAT('REF-', UUID())
        );
        SET i = i + 1;
    END WHILE;

    -- Lấy ID user range
    SELECT MIN(id), MAX(id) INTO user_min, user_max FROM k_users ORDER BY id DESC LIMIT in_user_count;

    -- 2. Tạo drivers đồng bộ user + driver
    SET i = 0;
    WHILE i < in_driver_count DO
        -- Tạo user trước
        INSERT INTO k_users (name, phone, email, password_hash, referral_code)
        VALUES (
            CONCAT('Driver_', i),
            CONCAT('091', LPAD(FLOOR(RAND() * 10000000), 7, '0')),
            CONCAT('driver', i, '@mail.com'),
            MD5(RAND()),
            CONCAT('DRV-', UUID())
        );
        SET new_user_id = LAST_INSERT_ID();

        -- Tạo driver tương ứng
        INSERT INTO k_drivers (
            id, license_number, car_plate, car_type, rating
        )
        VALUES (
            new_user_id,
            CONCAT('LIC', FLOOR(RAND()*100000)),
            CONCAT('PLATE', FLOOR(RAND()*10000)),
            in_car_type,
            ROUND(RAND()*1.5 + 3.5, 2)
        );

        SET i = i + 1;
    END WHILE;

    -- Lấy ID driver range
    SELECT MIN(id), MAX(id) INTO driver_min, driver_max FROM k_drivers ORDER BY id DESC LIMIT in_driver_count;

    -- 3. Tạo rides ngẫu nhiên giữa user & driver
    SET i = 0;
    WHILE i < in_ride_count DO
        SET rand_user = FLOOR(RAND() * (user_max - user_min + 1)) + user_min;
        SET rand_driver = FLOOR(RAND() * (driver_max - driver_min + 1)) + driver_min;
        SET ride_status = ELT(FLOOR(1 + (RAND() * 4)), 'requested','confirmed','completed','cancelled');
        SET rand_fare = ROUND(RAND()*300 + 50, 2);
        SET rand_distance = ROUND(RAND()*20 + 1, 2);
        SET reward_used = FLOOR(RAND()*100);
        SET base_time = TIMESTAMP(in_ride_date, SEC_TO_TIME(FLOOR(RAND()*86400)));
        SET start_loc = CONCAT('Start_', FLOOR(RAND()*100));
        SET end_loc = CONCAT('End_', FLOOR(RAND()*100));

        -- Insert ride
        INSERT INTO k_rides (
            user_id, driver_id, car_type, start_location, end_location, fare, status,
            requested_at, confirmed_at, completed_at, cancelled_at,
            reward_points_used, distance_km
        )
        VALUES (
            rand_user, rand_driver, in_car_type,
            start_loc, end_loc, rand_fare, ride_status,
            base_time,
            IF(ride_status IN ('confirmed','completed'), base_time + INTERVAL 1 MINUTE, NULL),
            IF(ride_status = 'completed', base_time + INTERVAL 15 MINUTE, NULL),
            IF(ride_status = 'cancelled', base_time + INTERVAL 5 MINUTE, NULL),
            reward_used,
            rand_distance
        );

        SET i = i + 1;
    END WHILE;
END;

-- Stored Procedure: tạo thưởng hằng ngày cho các chuyến completed hôm qua
Drop PROCEDURE if exists sp_process_daily_rewards;
CREATE PROCEDURE sp_process_daily_rewards()
BEGIN
    DECLARE start_time DATETIME;
    DECLARE end_time DATETIME;

    SET start_time = DATE_SUB(CURDATE(), INTERVAL 1 DAY);
    SET end_time = CURDATE();

    INSERT INTO reward_transactions(user_id, points, description)
    SELECT 
        r.user_id,
        calc_reward_points(r.distance_km),
        CONCAT('Reward for ride ID ', r.id)
    FROM rides r
    WHERE r.status = 'COMPLETED'
      AND r.completed_at BETWEEN start_time AND end_time;
END;

-- Tạo dữ liệu ngẫu nhiên cho chuyến đi
DROP PROCEDURE IF EXISTS sp_insert_random_k_rides;
CREATE PROCEDURE sp_insert_random_k_rides(
    IN in_target_date DATE,
    IN in_row_count INT,
    IN in_car_type VARCHAR(10),
    IN in_user_id_min BIGINT,
    IN in_user_id_max BIGINT,
    IN in_driver_id_min BIGINT,
    IN in_driver_id_max BIGINT
)
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE rand_user BIGINT;
    DECLARE rand_driver BIGINT;
    DECLARE ride_status ENUM('requested','confirmed','completed','cancelled');
    DECLARE rand_fare DECIMAL(10,2);
    DECLARE rand_distance DOUBLE;
    DECLARE reward_used INT;
    DECLARE start_loc VARCHAR(255);
    DECLARE end_loc VARCHAR(255);
    DECLARE base_datetime DATETIME;
    DECLARE try_count INT DEFAULT 0;
    DECLARE max_try INT DEFAULT 1000;

    WHILE i < in_row_count AND try_count < max_try DO
        SET try_count = try_count + 1;

        -- Random user và driver từ khoảng
        SET rand_user = FLOOR(RAND() * (in_user_id_max - in_user_id_min + 1)) + in_user_id_min;
        SET rand_driver = FLOOR(RAND() * (in_driver_id_max - in_driver_id_min + 1)) + in_driver_id_min;

        -- Kiểm tra cả user và driver phải tồn tại trong k_users, và driver phải có trong k_drivers
        IF EXISTS (SELECT 1 FROM k_users WHERE id = rand_user)
           AND EXISTS (
               SELECT 1
               FROM k_users u
               JOIN k_drivers d ON u.id = d.id
               WHERE u.id = rand_driver
           )
        THEN
            -- Tạo dữ liệu ride
            SET ride_status = ELT(FLOOR(1 + (RAND() * 4)), 'requested','confirmed','completed','cancelled');
            SET rand_fare = ROUND(RAND() * 500 + 20, 2);
            SET rand_distance = ROUND(RAND() * 30 + 1, 2);
            SET reward_used = FLOOR(RAND() * 50);
            SET base_datetime = TIMESTAMP(in_target_date, SEC_TO_TIME(FLOOR(RAND()*86400)));
            SET start_loc = CONCAT('Start-', FLOOR(RAND() * 100));
            SET end_loc = CONCAT('End-', FLOOR(RAND() * 100));

            -- Nếu ride này đã tồn tại (cùng user, driver, thời điểm), thì update
            IF EXISTS (
                SELECT 1 FROM k_rides
                WHERE user_id = rand_user
                  AND driver_id = rand_driver
                  AND requested_at = base_datetime
            ) THEN
                UPDATE k_rides
                SET 
                    car_type = in_car_type,
                    start_location = start_loc,
                    end_location = end_loc,
                    fare = rand_fare,
                    status = ride_status,
                    confirmed_at = IF(ride_status IN ('confirmed','completed'), base_datetime + INTERVAL 1 MINUTE, NULL),
                    completed_at = IF(ride_status = 'completed', base_datetime + INTERVAL 15 MINUTE, NULL),
                    cancelled_at = IF(ride_status = 'cancelled', base_datetime + INTERVAL 5 MINUTE, NULL),
                    reward_points_used = reward_used,
                    distance_km = rand_distance
                WHERE user_id = rand_user 
                  AND driver_id = rand_driver 
                  AND requested_at = base_datetime;
            ELSE
                -- Insert mới nếu chưa tồn tại
                INSERT INTO k_rides (
                    user_id, driver_id, car_type, start_location, end_location, fare, status,
                    requested_at, confirmed_at, completed_at, cancelled_at, reward_points_used, distance_km
                )
                VALUES (
                    rand_user, rand_driver, in_car_type, start_loc, end_loc, rand_fare, ride_status,
                    base_datetime,
                    IF(ride_status IN ('confirmed','completed'), base_datetime + INTERVAL 1 MINUTE, NULL),
                    IF(ride_status = 'completed', base_datetime + INTERVAL 15 MINUTE, NULL),
                    IF(ride_status = 'cancelled', base_datetime + INTERVAL 5 MINUTE, NULL),
                    reward_used,
                    rand_distance
                );
            END IF;

            -- Tăng số lượt thành công
            SET i = i + 1;
        END IF;
    END WHILE;
END;

-- Sinh lịch sử thanh toán ngẫu nhiên 
drop procedure if exists sp_insert_random_payments;
CREATE PROCEDURE sp_insert_random_payments(
    IN in_payment_count INT,
    IN in_payment_date DATE
)
BEGIN
    -- Gán nhãn 1 lần duy nhất cho khối BEGIN ... END
    proc: BEGIN
        DECLARE i INT DEFAULT 0;
        DECLARE rand_idx INT;
        DECLARE total_rides INT;
        DECLARE r_id BIGINT;
        DECLARE u_id BIGINT;
        DECLARE p_amount DECIMAL(10,2);
        DECLARE p_method ENUM('cash','card','momo','zalopay');
        DECLARE p_status ENUM('pending','success','failed');
        DECLARE p_time DATETIME;

        -- Tạo bảng tạm chứa danh sách ride đủ điều kiện
        CREATE TEMPORARY TABLE IF NOT EXISTS tmp_valid_rides AS
        SELECT id AS ride_id, user_id, fare
        FROM k_rides
        WHERE fare >= 10;

        -- Đếm số lượng rides hợp lệ
        SELECT COUNT(*) INTO total_rides FROM tmp_valid_rides;

        -- Nếu không có ride hợp lệ thì kết thúc
        IF total_rides = 0 THEN
            LEAVE proc;
        END IF;

        WHILE i < in_payment_count DO
            -- Random chỉ số trong danh sách
            SET rand_idx = FLOOR(RAND() * total_rides);

            -- Lấy ride tại vị trí rand_idx
            SELECT ride_id, user_id, fare
            INTO r_id, u_id, p_amount
            FROM tmp_valid_rides
            LIMIT 1 OFFSET rand_idx;

            -- Random lại số tiền từ fare
            SET p_amount = ROUND(RAND() * (p_amount - 10) + 10, 2);
            SET p_method = ELT(FLOOR(1 + (RAND() * 4)), 'cash','card','momo','zalopay');
            SET p_status = ELT(FLOOR(1 + (RAND() * 3)), 'pending','success','failed');
            SET p_time = TIMESTAMP(in_payment_date, SEC_TO_TIME(FLOOR(RAND() * 86400)));

            -- Chèn thanh toán
            INSERT INTO k_payments (
                ride_id, user_id, amount, payment_method, paid_at, status, transaction_ref
            )
            VALUES (
                r_id, u_id, p_amount, p_method, p_time, p_status, CONCAT('TXN-', UUID())
            );

            SET i = i + 1;
        END WHILE;

        -- Dọn bảng tạm
        DROP TEMPORARY TABLE IF EXISTS tmp_valid_rides;
    END proc;
END;

DROP PROCEDURE IF EXISTS sp_insert_random_referrals;
CREATE PROCEDURE sp_insert_random_referrals(
    IN in_count INT
)
BEGIN
proc: BEGIN 
    DECLARE i INT DEFAULT 0;
    DECLARE total_users INT;
    DECLARE rand_referrer BIGINT;
    DECLARE rand_referred BIGINT;
    DECLARE ref_min BIGINT;
    DECLARE ref_max BIGINT;
    DECLARE attempt INT DEFAULT 0;
    DECLARE max_attempt INT DEFAULT 1000;

    -- Lấy khoảng ID hợp lệ từ k_users
    SELECT MIN(id), MAX(id) INTO ref_min, ref_max FROM k_users;
    SELECT COUNT(*) INTO total_users FROM k_users;

    -- Nếu quá ít user, thì dừng
    IF total_users < 2 THEN
        LEAVE proc;
    END IF;

    WHILE i < in_count AND attempt < max_attempt DO
        SET attempt = attempt + 1;

        -- Random 2 user khác nhau
        SET rand_referrer = FLOOR(RAND() * (ref_max - ref_min + 1)) + ref_min;
        SET rand_referred = FLOOR(RAND() * (ref_max - ref_min + 1)) + ref_min;

        -- Không cho tự giới thiệu
        IF rand_referrer <> rand_referred THEN

            -- Kiểm tra cả 2 user tồn tại và chưa có bản ghi referral này
            IF EXISTS (SELECT 1 FROM k_users WHERE id = rand_referrer)
               AND EXISTS (SELECT 1 FROM k_users WHERE id = rand_referred)
               AND NOT EXISTS (
                   SELECT 1 FROM k_referrals 
                   WHERE referrer_id = rand_referrer AND referee_id = rand_referred
               )
            THEN
                INSERT INTO k_referrals (
                    referrer_id, referee_id, created_at
                )
                VALUES (
                    rand_referrer, rand_referred, NOW()
                );
                SET i = i + 1;
            END IF;
        END IF;
    END WHILE;

    END proc ;
END;

-- Index hỗ trợ truy vấn theo ngày
CREATE INDEX idx_rides_completed_at ON k_rides(completed_at);
CREATE INDEX idx_rides_status ON k_rides(status);

