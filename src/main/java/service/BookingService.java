package service;

import model.entity.Booking;
import model.entity.Ticket;
import model.entity.User;
import repository.BookingRepository;
import repository.ShowtimeRepository;
import repository.TicketRepository;
import repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private ShowtimeRepository showtimeRepository;

    @Transactional
    public void createBooking(User user, Long showtimeId, List<Long> seatIds) throws Exception {
        Booking booking = new Booking();
        booking.setUser(user);

        // Cần tạo enum BookingStatus nếu có, ví dụ status
        // booking.setStatus(BookingStatus.CONFIRMED);

        bookingRepository.save(booking);

        for (Long seatId : seatIds) {
            // Kiểm tra CORE-06 chống double booking
            if (ticketRepository.existsByShowtimeIdAndSeatId(showtimeId, seatId)) {
                throw new Exception("Ghế đã được đặt bởi người khác! Vui lòng chọn ghế khác.");
            }

            Ticket ticket = new Ticket();
            ticket.setBooking(booking);

            // Placeholder to show context
            // ticket.setShowtime(showtimeRepository.findById(showtimeId).get());
            // ticket.setSeat(seatRepository.findById(seatId).get());

            ticketRepository.save(ticket);
        }
    }

    @Transactional
    public void cancelBooking(Long bookingId, Long expectedUserId) throws Exception {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new Exception("Không tìm thấy đơn hàng"));

        if (!booking.getUser().getId().equals(expectedUserId)) {
            throw new Exception("Không có quyền hủy vé này");
        }
        if (booking.getStatus() == model.entity.BookingStatus.CANCELLED) {
            throw new Exception("Vé đã được hủy trước đó");
        }

        // Lấy suất chiếu từ tick đầu tiên (giả sử 1 booking = 1 suất chiếu)
        if (booking.getTickets() != null && !booking.getTickets().isEmpty()) {
            LocalDateTime startTime = booking.getTickets().get(0).getShowtime().getStartTime();
            if (LocalDateTime.now().isAfter(startTime.minusHours(24))) {
                throw new Exception("Chỉ có thể hủy vé trước 24h so với giờ chiếu");
            }
        }

        booking.setStatus(model.entity.BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        // Core 09: Giải phóng Slot bằng cách xóa chi tiết vé để người khác có thể đặt lại
        ticketRepository.deleteAll(booking.getTickets());
    }
}
