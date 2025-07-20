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
END