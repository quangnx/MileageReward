-- bảng tài xế
CREATE TABLE k_drivers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(100),
    license_number VARCHAR(50),
    car_plate VARCHAR(20),
    car_type VARCHAR(20),
    rating DECIMAL(3,2) DEFAULT 5.00,
    joined_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    status ENUM('active', 'inactive', 'banned') DEFAULT 'active'
);