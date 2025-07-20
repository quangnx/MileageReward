DROP PROCEDURE IF EXISTS sp_insert_bulk_users_drivers_rides;
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
    DECLARE first_driver_id BIGINT;

    -- 1. Tạo Users
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

    -- Lấy khoảng ID users vừa insert
    SELECT MIN(id), MAX(id) INTO user_min, user_max
    FROM (
        SELECT id FROM k_users ORDER BY id DESC LIMIT in_user_count
    ) AS recent_users;

    -- 2. Tạo Drivers (đồng bộ user + driver)
    -- Insert driver đầu tiên, lấy first_driver_id
    INSERT INTO k_users (name, phone, email, password_hash, referral_code)
    VALUES (
        'Driver_0',
        CONCAT('091', LPAD(FLOOR(RAND() * 10000000), 7, '0')),
        'driver0@mail.com',
        MD5(RAND()),
        CONCAT('DRV-', UUID())
    );
    SET first_driver_id = LAST_INSERT_ID();

    INSERT INTO k_drivers (
        id, license_number, car_plate, car_type, rating
    )
    VALUES (
        first_driver_id,
        CONCAT('LIC', FLOOR(RAND()*100000)),
        CONCAT('PLATE', FLOOR(RAND()*10000)),
        in_car_type,
        ROUND(RAND()*1.5 + 3.5, 2)
    );

    -- Insert các driver còn lại
    SET i = 1;
    WHILE i < in_driver_count DO
        INSERT INTO k_users (name, phone, email, password_hash, referral_code)
        VALUES (
            CONCAT('Driver_', i),
            CONCAT('091', LPAD(FLOOR(RAND() * 10000000), 7, '0')),
            CONCAT('driver', i, '@mail.com'),
            MD5(RAND()),
            CONCAT('DRV-', UUID())
        );
        SET new_user_id = LAST_INSERT_ID();

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

    -- Ghi lại khoảng driver_id từ first_driver_id đến id mới nhất
    SELECT MAX(id) INTO driver_max FROM k_users;
    SET driver_min = first_driver_id;

    -- 3. Tạo rides
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
