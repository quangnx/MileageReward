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
