1. Hệ thống Người dùng (User & Profile)
   users (1) - (1) user_profiles (Quan hệ 1-1)
   Mỗi tài khoản đăng nhập (users) chỉ được phép có duy nhất 1 hồ sơ chi tiết (user_profiles). 
Điều này được thể hiện qua khoá phụ user_id có kèm theo ràng buộc UNIQUE ở bảng user_profiles.
2. Hệ thống Cơ sở vật chất (Rạp & Ghế)
   rooms (1) - (N) seats (Quan hệ 1-Nhiều)
   Một phòng chiếu (rooms) sẽ bao gồm nhiều ghế ngồi (seats). Bảng ghế lưu lại room_id nó thuộc về. 
Đặc biệt, có một ràng buộc unique_seat_in_room ở bảng seats để đảm bảo không một phòng nào có hai ghế trùng dãy 
và trùng số (Ví dụ: Không thể có 2 ghế A1 ở trong cùng 1 phòng).
3. Hệ thống Phim ảnh (Movie & Genre)
   movies (N) - (N) genres (Quan hệ Nhiều-Nhiều)
   Một bộ phim có thể mang nhiều thể loại (Hành động, Hài, Tình cảm...) và ngược lại một thể loại có nhiều phim.
   Mối quan hệ này được giải quyết thông qua việc thêm một bảng trung gian 
là movie_genres (gồm 2 khóa ngoại: movie_id và genre_id).
4. Hệ thống Suất chiếu (Showtimes)
   Bảng Suất chiếu showtimes chính là tâm điểm kết nối phim và rạp, chúng có quan hệ 1-N với 2 thực thể:
   movies (1) - (N) showtimes: Một bộ phim có thể được chiểu ở nhiều khoảng thời gian (nhiều suất chiếu) khác nhau.
   rooms (1) - (N) showtimes: Một phòng chiếu phục vụ nhiều suất chiếu tại các mốc thời gian khác nhau.
5. Hệ thống Đặt vé và Thanh toán (Booking & Tickets)
   users (1) - (N) bookings (Quan hệ 1-Nhiều)
   Một khách hàng (users) có thể thực hiện nhiều lượt đặt vé/hóa đơn (bookings).
   bookings (1) - (N) tickets (Quan hệ 1-Nhiều)
   Trong một lượt đặt vé (bookings), khách hàng có thể mua cùng lúc nhiều vé / chọn nhiều ghế.
Mỗi ghế tương ứng với một bản ghi chi tiết riêng rẽ trong bảng tickets.
   tickets tương tác với showtimes và seats
   Mỗi tấm vé (tickets) bắt buộc phải gắn liền tới 1 suất chiếu cụ thể (showtime_id) và chính xác 1 ghế duy nhất (seat_i