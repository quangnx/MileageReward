-- Batch SQL script to generate sample data
-- ==========================
-- PART 1: DECLARE INPUT VARIABLES
-- ==========================
SET @number_user = 100;            -- Number of users
SET @number_driver = 50;           -- Number of drivers
SET @number_ride = 100;            -- Number of rides
SET @number_payment = 80;          -- Number of payment transactions
SET @ride_date = CURDATE();        -- Ride date (set specific DATE if needed)
SET @car_type_main = '4S';         -- Default car type
SET @car_type_alt = '7S';          -- Alternate car type
SET @user_id_min = 1;              -- User ID range >> check table for min/max
SET @user_id_max = 100;
SET @driver_id_min = 1;            -- Driver ID range >> check table for min/max
SET @driver_id_max = 50;

-- ==========================
-- PART 2: CALL DATA GENERATION PROCEDURES
--          CAN BE CALLED MULTIPLE TIMES
-- ==========================

-- Generate users and random rides
CALL sp_insert_bulk_users_drivers_rides(
    @number_user,
    @number_driver,
    @number_ride,
    @ride_date,
    @car_type_main
);

-- Generate payment transactions for rides
CALL sp_insert_random_payments(
    @number_payment,
    @ride_date
);

-- Generate more rides with a specific user-driver range
CALL sp_insert_random_k_rides(
    @ride_date,
    20,                  -- additional rides
    @car_type_alt,
    @user_id_min,
    @user_id_max,
    @driver_id_min,
    @driver_id_max
);

-- Add referral records
call sp_insert_random_referrals(@refferal_num);
