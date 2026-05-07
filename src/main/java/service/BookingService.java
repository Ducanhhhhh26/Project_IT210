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
        booking.setBookingDate(LocalDateTime.now());

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
}

