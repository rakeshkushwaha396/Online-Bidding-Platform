create database biddata;

use biddata;

select * from products;
select * from users;
delete from users where username='Ayush1';
desc users;




INSERT INTO admins (username, password) VALUES ('admin', 'admin@123');


SELECT * FROM admins WHERE username = 'admin';
INSERT INTO admins (username, password) VALUES ('admin2', 'admin123');

drop database biddata;
-- --------------------------------------------
-- ===============================
-- USERS TABLE
-- ===============================
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(150) NOT NULL UNIQUE,
    fullname VARCHAR(150),
    password VARCHAR(255),
    mobile VARCHAR(20) NOT NULL UNIQUE,
    aadhaar_no VARCHAR(20) NOT NULL UNIQUE,
    aadhaar_photo VARCHAR(255),
    pan_no VARCHAR(20) NOT NULL UNIQUE,
    pan_photo VARCHAR(255),
    is18plus BOOLEAN DEFAULT FALSE,
    is_verified BOOLEAN DEFAULT FALSE,
    email_otp VARCHAR(10),
    otp_verified BOOLEAN DEFAULT FALSE
);

-- ===============================
-- ADMINS TABLE
-- ===============================
CREATE TABLE admins (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

-- ===============================
-- PRODUCTS TABLE
-- ===============================
CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    image VARCHAR(255) NOT NULL,
    base_price DOUBLE NOT NULL,
    auction_start DATETIME NOT NULL,
    auction_end DATETIME NOT NULL,
    status VARCHAR(50) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
DELETE FROM products;
-- ===============================
-- BIDS TABLE
-- ===============================
CREATE TABLE bids (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    bid_price DOUBLE NOT NULL,
    bid_time DATETIME NOT NULL,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ===============================
-- PAYMENTS TABLE
-- ===============================
CREATE TABLE payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    amount DOUBLE NOT NULL,
    status VARCHAR(50) NOT NULL,
    razorpay_order_id VARCHAR(100),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);




INSERT INTO users (username, email, fullname, password, mobile, aadhaar_no, aadhaar_photo, pan_no, pan_photo, is18plus, is_verified, email_otp, otp_verified) VALUES
('john_doe', 'john@example.com', 'John Doe', 'pass123', '9876543210', '123456789012', 'aadhaar1.jpg', 'ABCDE1234F', 'pan1.jpg', TRUE, TRUE, '123456', TRUE),
('jane_smith', 'jane@example.com', 'Jane Smith', 'pass123', '9876543211', '123456789013', 'aadhaar2.jpg', 'ABCDE1235F', 'pan2.jpg', TRUE, TRUE, '234567', TRUE),
('mike_jordan', 'mike@example.com', 'Mike Jordan', 'pass123', '9876543212', '123456789014', 'aadhaar3.jpg', 'ABCDE1236F', 'pan3.jpg', TRUE, FALSE, '345678', FALSE),
('alice_wonder', 'alice@example.com', 'Alice Wonder', 'pass123', '9876543213', '123456789015', 'aadhaar4.jpg', 'ABCDE1237F', 'pan4.jpg', TRUE, TRUE, '456789', TRUE),
('bob_builder', 'bob@example.com', 'Bob Builder', 'pass123', '9876543214', '123456789016', 'aadhaar5.jpg', 'ABCDE1238F', 'pan5.jpg', TRUE, TRUE, '567890', TRUE);





INSERT INTO bids (product_id, user_id, bid_price, bid_time) VALUES
(1, 2, 52000, '2025-07-23 11:00:00'),
(1, 3, 53000, '2025-07-23 12:00:00'),
(2, 1, 81000, '2025-07-24 10:00:00'),
(3, 4, 16000, '2025-07-23 13:00:00'),
(4, 5, 42000, '2025-07-23 16:00:00');


INSERT INTO payments (user_id, product_id, amount, status, razorpay_order_id) VALUES
(3, 1, 53000, 'SUCCESS', 'order_xyz_001'),
(1, 2, 81000, 'SUCCESS', 'order_xyz_002'),
(4, 3, 16000, 'PENDING', 'order_xyz_003'),
(5, 4, 42000, 'SUCCESS', 'order_xyz_004'),
(2, 5, 31000, 'FAILED', 'order_xyz_005');



INSERT INTO products (user_id, name, description, image, base_price, auction_start, auction_end, status) 
VALUES 
(1, 'Vintage Camera', 'Classic 35mm film camera from the 1970s in excellent condition.', 'https://images.unsplash.com/photo-1510127034890-ba27508e9f1c?w=600', 120.00, '2023-11-01 09:00:00', '2023-11-08 18:00:00', 'active'),

(2, 'Handmade Wooden Chair', 'Beautiful oak chair handcrafted by local artisans.', 'https://images.unsplash.com/photo-1567538096630-e0c55bd6374c?w=600', 85.50, '2023-11-02 10:00:00', '2023-11-09 20:00:00', 'active'),

(3, 'Smart Watch', 'Latest model smart watch with fitness tracking and notifications.', 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600', 199.99, '2023-11-03 08:00:00', '2023-11-10 17:00:00', 'active'),

(1, 'Antique Pocket Watch', 'Rare 19th century pocket watch with intricate engravings.', 'https://images.unsplash.com/photo-1524805444758-089113d48a6d?w=600', 350.00, '2023-11-04 11:00:00', '2023-11-11 19:00:00', 'active'),

(2, 'Oil Painting Landscape', 'Original oil painting of countryside landscape, 24x36 inches.', 'https://images.unsplash.com/photo-1579783902614-a3fb3927b6a5?w=600', 275.00, '2023-11-05 09:30:00', '2023-11-12 18:30:00', 'active'),

(3, 'Leather Messenger Bag', 'Genuine leather bag with multiple compartments, perfect for work.', 'https://images.unsplash.com/photo-1548036325-c9d93f03e0c8?w=600', 129.95, '2023-11-06 10:00:00', '2023-11-13 20:00:00', 'active');