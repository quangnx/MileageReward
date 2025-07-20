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
