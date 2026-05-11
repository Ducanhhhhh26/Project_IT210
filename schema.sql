-- X�a database n?u d� t?n t?i r?i t?o l?i (X�A TO�N B? D? LI?U)
DROP DATABASE IF EXISTS CinemaBooking;
CREATE DATABASE CinemaBooking CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE CinemaBooking;

-- 1. users
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    role ENUM('ADMIN', 'STAFF', 'CUSTOMER') NOT NULL DEFAULT 'CUSTOMER'
);

-- 2. user_profiles
CREATE TABLE user_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    full_name VARCHAR(100),
    phone VARCHAR(20),
    address TEXT,
    avatar VARCHAR(255),
    CONSTRAINT fk_user_profile FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 3. rooms
CREATE TABLE rooms (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    capacity INT NOT NULL
);

-- 4. seats
CREATE TABLE seats (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_id BIGINT NOT NULL,
    row_name VARCHAR(5) NOT NULL,
    seat_name VARCHAR(10),
    CONSTRAINT fk_seat_room FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE
);

-- 5. genres
CREATE TABLE genres (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

-- 6. movies
CREATE TABLE movies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    duration INT NOT NULL,
    genre_id BIGINT NULL,
    release_date DATE,
    poster_url VARCHAR(255),
    CONSTRAINT fk_movie_genre FOREIGN KEY (genre_id) REFERENCES genres(id) ON DELETE SET NULL
);

-- 7. showtimes
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

-- 8. bookings
CREATE TABLE bookings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    booking_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(15, 2) NOT NULL DEFAULT 0,
    status ENUM('PENDING', 'CONFIRMED', 'CANCELLED') DEFAULT 'PENDING',
    CONSTRAINT fk_booking_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 9. tickets
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
