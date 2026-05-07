package controller;

import model.entity.Movie;
import model.entity.Showtime;
import repository.MovieRepository;
import repository.ShowtimeRepository;
import repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Controller
public class CustomerShowtimeController {

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private ShowtimeRepository showtimeRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @GetMapping("/movie/{id}/showtimes")
    public String viewShowtimes(@PathVariable Long id, Model model) {
        Movie movie = movieRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Mã phim không hợp lệ"));

        // CORE-08: Ẩn hoàn toàn nhưng suất chiếu quá giờ (Truy vấn currentTime)
        List<Showtime> availableShowtimes = showtimeRepository.findAvailableShowtimes(id, LocalDateTime.now());

        // CORE-08: Kiểm tra trạng thái Hết Vé (Sold out)
        List<Map<String, Object>> showtimeList = new ArrayList<>();
        for (Showtime st : availableShowtimes) {
            Map<String, Object> map = new HashMap<>();
            map.put("showtime", st);

            // Giả định có TicketRepository truy xuất List vé dựa vào showtimeId
            long totalTicketsBooked = ticketRepository.findByShowtimeId(st.getId()).size();
            long roomCapacity = st.getRoom().getCapacity(); // Lấy tổng ghế của phòng

            boolean isSoldOut = totalTicketsBooked >= roomCapacity;
            map.put("isSoldOut", isSoldOut);
            map.put("availableSeats", roomCapacity - totalTicketsBooked);

            showtimeList.add(map);
        }

        model.addAttribute("movie", movie);
        model.addAttribute("showtimeList", showtimeList);
        return "customer/showtimes";
    }
}

