package service;

import model.entity.Booking;
import model.entity.BookingStatus;
import model.entity.Seat;
import model.entity.Showtime;
import model.entity.Ticket;
import model.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.BookingRepository;
import repository.SeatRepository;
import repository.ShowtimeRepository;
import repository.TicketRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private static final BigDecimal TICKET_PRICE = new BigDecimal("75000");

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private ShowtimeRepository showtimeRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Transactional
    public Booking createBooking(User user, Long showtimeId, List<Long> seatIds) throws Exception {
        if (seatIds == null || seatIds.isEmpty()) {
            throw new Exception("Vui long chon it nhat 1 ghe.");
        }

        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new Exception("Khong tim thay suat chieu"));

        if (!showtime.getStartTime().isAfter(LocalDateTime.now())) {
            throw new Exception("Suat chieu da bat dau hoac da ket thuc.");
        }

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setStatus(BookingStatus.PENDING);
        booking.setTotalPrice(TICKET_PRICE.multiply(BigDecimal.valueOf(seatIds.size())));
        bookingRepository.save(booking);

        for (Long seatId : seatIds) {
            Seat seat = seatRepository.findById(seatId)
                    .orElseThrow(() -> new Exception("Khong tim thay ghe"));

            if (!seat.getRoom().getId().equals(showtime.getRoom().getId())) {
                throw new Exception("Ghe khong thuoc phong chieu cua suat nay.");
            }

            if (ticketRepository.existsActiveTicket(showtimeId, seatId)) {
                throw new Exception("Ghe da duoc dat boi nguoi khac. Vui long chon ghe khac.");
            }

            Ticket ticket = new Ticket();
            ticket.setBooking(booking);
            ticket.setShowtime(showtime);
            ticket.setSeat(seat);
            ticket.setPrice(TICKET_PRICE);
            ticketRepository.save(ticket);
        }

        return booking;
    }

    @Transactional(readOnly = true)
    public Booking getBookingForUser(Long bookingId, Long userId) throws Exception {
        Booking booking = bookingRepository.findByIdWithTickets(bookingId);
        if (booking == null) {
            throw new Exception("Khong tim thay don hang");
        }
        if (!booking.getUser().getId().equals(userId)) {
            throw new Exception("Ban khong co quyen truy cap don hang nay");
        }
        return booking;
    }

    @Transactional
    public void confirmPayment(Long bookingId, Long userId) throws Exception {
        Booking booking = getBookingForUser(bookingId, userId);
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new Exception("Don hang da bi huy");
        }
        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            return;
        }
        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);
    }

    @Transactional(readOnly = true)
    public List<BookingHistoryItem> getBookingHistory(Long userId) {
        return bookingRepository.findHistoryByUserId(userId).stream()
                .map(this::toHistoryItem)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ShowtimeAvailability> getAvailableShowtimes(Long movieId) {
        return showtimeRepository.findAvailableShowtimes(movieId, LocalDateTime.now()).stream()
                .map(showtime -> {
                    long bookedSeats = ticketRepository.countActiveByShowtimeId(showtime.getId());
                    long capacity = showtime.getRoom().getCapacity();
                    long availableSeats = Math.max(0, capacity - bookedSeats);
                    return new ShowtimeAvailability(showtime, bookedSeats, availableSeats, availableSeats == 0);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Seat> getSeatsForShowtime(Long showtimeId) throws Exception {
        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new Exception("Khong tim thay suat chieu"));
        return seatRepository.findByRoomIdOrderBySeatNameAsc(showtime.getRoom().getId());
    }

    @Transactional(readOnly = true)
    public Showtime getShowtime(Long showtimeId) throws Exception {
        return showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new Exception("Khong tim thay suat chieu"));
    }

    @Transactional(readOnly = true)
    public List<Long> getBookedSeatIds(Long showtimeId) {
        return ticketRepository.findActiveByShowtimeId(showtimeId).stream()
                .map(ticket -> ticket.getSeat().getId())
                .collect(Collectors.toList());
    }

    @Transactional
    public void cancelBooking(Long bookingId, Long expectedUserId) throws Exception {
        Booking booking = bookingRepository.findByIdWithTickets(bookingId);
        if (booking == null) {
            throw new Exception("Khong tim thay don hang");
        }

        if (!booking.getUser().getId().equals(expectedUserId)) {
            throw new Exception("Khong co quyen huy ve nay");
        }
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new Exception("Ve da duoc huy truoc do");
        }

        if (booking.getTickets() != null && !booking.getTickets().isEmpty()) {
            LocalDateTime startTime = booking.getTickets().get(0).getShowtime().getStartTime();
            if (!LocalDateTime.now().isBefore(startTime)) {
                throw new Exception("Khong the huy ve khi suat chieu da bat dau hoac da ket thuc");
            }
        }
//     if (LocalDateTime.now().isAfter(startTime.minusHours(24))) {
//         throw new Exception("Chỉ được hủy vé trước 24h so với giờ chiếu!");
//     }
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }

    @Transactional(readOnly = true)
    public Booking getBookingForStaffByCode(String bookingCode) throws Exception {
        Long bookingId = parseBookingCode(bookingCode);
        Booking booking = bookingRepository.findByIdWithTickets(bookingId);
        if (booking == null) {
            throw new Exception("Khong tim thay don hang voi ma " + bookingCode);
        }
        return booking;
    }

    @Transactional(readOnly = true)
    public List<Booking> getRecentBookingsForStaff(int limit) {
        if (limit <= 0) {
            return Collections.emptyList();
        }
        List<Long> ids = bookingRepository.findRecentBookingIds(PageRequest.of(0, limit));
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }
        return bookingRepository.findByIdsWithTicketsOrderByCreatedAtDesc(ids);
    }

    public String toBookingCode(Long bookingId) {
        if (bookingId == null) {
            return "N/A";
        }
        return String.format("BKG-%06d", bookingId);
    }

    private Long parseBookingCode(String rawCode) throws Exception {
        if (rawCode == null || rawCode.trim().isEmpty()) {
            throw new Exception("Vui long nhap ma don hang");
        }

        String normalized = rawCode.trim().toUpperCase();
        if (normalized.startsWith("BKG-")) {
            normalized = normalized.substring(4);
        }

        if (!normalized.matches("\\d+")) {
            throw new Exception("Ma don hang khong hop le. Dinh dang dung: BKG-000123");
        }

        return Long.parseLong(normalized);
    }

    private BookingHistoryItem toHistoryItem(Booking booking) {
        BookingHistoryItem item = new BookingHistoryItem();
        item.setBookingId(booking.getId());
        item.setStatus(booking.getStatus());
        item.setTotalPrice(booking.getTotalPrice());

        List<Ticket> tickets = booking.getTickets() == null ? new ArrayList<>() : booking.getTickets();
        item.setSeatNames(tickets.stream()
                .map(ticket -> ticket.getSeat().getSeatName())
                .collect(Collectors.toList()));

        if (!tickets.isEmpty()) {
            Showtime showtime = tickets.get(0).getShowtime();
            item.setMovieTitle(showtime.getMovie().getTitle());
            item.setRoomName(showtime.getRoom().getName());
            item.setShowtimeStart(showtime.getStartTime());
            item.setCancellable(booking.getStatus() != BookingStatus.CANCELLED
                    && LocalDateTime.now().isBefore(showtime.getStartTime()));
        } else {
            item.setMovieTitle("N/A");
            item.setRoomName("N/A");
            item.setCancellable(false);
        }

        return item;
    }
}
