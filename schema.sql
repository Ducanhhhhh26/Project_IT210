-- Xóa database nếu đã tồn tại để tạo lại (Cẩn thận khi chạy trên production)
CREATE DATABASE IF NOT EXISTS scbs_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE scbs_db;

-- 1. BẢNG USERS (Lưu thông tin đăng nhập và phân quyền)
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL, -- Sẽ lưu mã băm (Hash)
    role ENUM('ADMIN', 'STAFF', 'CUSTOMER') NOT NULL DEFAULT 'CUSTOMER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. BẢNG USER_PROFILES (Hồ sơ người dùng chi tiết)
CREATE TABLE user_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    full_name VARCHAR(100),
    phone VARCHAR(20),
    address TEXT,
    avatar VARCHAR(255),
    CONSTRAINT fk_user_profile FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 3. BẢNG ROOMS (Phòng chiếu - Seed data)
CREATE TABLE rooms (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    capacity INT NOT NULL
);

-- 4. BẢNG SEATS (Ghế ngồi - Seed data)
CREATE TABLE seats (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_id BIGINT NOT NULL,
    row_name VARCHAR(5) NOT NULL,   -- Ví dụ: A, B, C
    seat_number INT NOT NULL,       -- Ví dụ: 1, 2, 3
    CONSTRAINT fk_seat_room FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE,
    UNIQUE KEY unique_seat_in_room (room_id, row_name, seat_number) -- Đảm bảo không có 2 ghế trùng nhau trong 1 phòng
);

-- 5. BẢNG GENRES (Thể loại phim - Seed data)
CREATE TABLE genres (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

-- 6. BẢNG MOVIES (Thông tin phim)
CREATE TABLE movies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    duration_minutes INT NOT NULL,  -- Rất quan trọng để tính thời gian kết thúc suất chiếu
    release_date DATE,
    poster_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 7. BẢNG MOVIE_GENRES (Bảng trung gian N-N giữa Movies và Genres)
CREATE TABLE movie_genres (
    movie_id BIGINT NOT NULL,
    genre_id BIGINT NOT NULL,
    PRIMARY KEY (movie_id, genre_id),
    CONSTRAINT fk_mg_movie FOREIGN KEY (movie_id) REFERENCES movies(id) ON DELETE CASCADE,
    CONSTRAINT fk_mg_genre FOREIGN KEY (genre_id) REFERENCES genres(id) ON DELETE CASCADE
);

-- 8. BẢNG SHOWTIMES (Suất chiếu)
CREATE TABLE showtimes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    movie_id BIGINT NOT NULL,
    room_id BIGINT NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    status ENUM('SCHEDULED', 'SHOWING', 'ENDED') DEFAULT 'SCHEDULED',
    CONSTRAINT fk_showtime_movie FOREIGN KEY (movie_id) REFERENCES movies(id),
    CONSTRAINT fk_showtime_room FOREIGN KEY (room_id) REFERENCES rooms(id)
);

-- 9. BẢNG BOOKINGS (Hóa đơn đặt vé tổng)
CREATE TABLE bookings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY, -- Có thể đổi sang VARCHAR(36) nếu dùng UUID trong Java
    user_id BIGINT NOT NULL,
    booking_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(15, 2) NOT NULL DEFAULT 0,
    status ENUM('PENDING', 'CONFIRMED', 'CANCELLED') DEFAULT 'PENDING',
    CONSTRAINT fk_booking_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 10. BẢNG TICKETS (Chi tiết từng vé)
CREATE TABLE tickets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    showtime_id BIGINT NOT NULL,
    seat_id BIGINT NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    CONSTRAINT fk_ticket_booking FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,
    CONSTRAINT fk_ticket_showtime FOREIGN KEY (showtime_id) REFERENCES showtimes(id),
    CONSTRAINT fk_ticket_seat FOREIGN KEY (seat_id) REFERENCES seats(id)
);

-- LƯU Ý VỀ RÀNG BUỘC KÉP CHO TICKETS (Double-booking):
-- Vì MySQL không hỗ trợ tốt Partial Unique Index (Ví dụ: Chỉ unique khi booking.status = 'CONFIRMED'),
-- nên ở cấp Database chúng ta không setup constraint UNIQUE(showtime_id, seat_id) trực tiếp ở đây,
-- mà sẽ dùng Lock (Pessimistic Lock) ở tầng Java Spring Boot JPA, kết hợp check logic:
-- "SELECT COUNT(*) FROM tickets t JOIN bookings b WHERE t.showtime_id = ? AND t.seat_id = ? AND b.status != 'CANCELLED'"

