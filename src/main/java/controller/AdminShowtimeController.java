package controller;

import model.entity.Movie;
import model.entity.Room;
import model.entity.Showtime;

import repository.MovieRepository;
import repository.RoomRepository;
import repository.ShowtimeRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.List;

@Controller // Đánh dấu đây là Controller của Spring MVC
@RequestMapping("/admin/showtimes") // URL gốc: /admin/showtimes
public class AdminShowtimeController {

    // Repository thao tác với bảng Showtime
    @Autowired
    private ShowtimeRepository showtimeRepository;

    // Repository thao tác với bảng Movie
    @Autowired
    private MovieRepository movieRepository;

    // Repository thao tác với bảng Room
    @Autowired
    private RoomRepository roomRepository;

    // =========================
    // HIỂN THỊ DANH SÁCH SUẤT CHIẾU
    // =========================
    @GetMapping
    public String listShowtimes(Model model) {

        // Lấy toàn bộ suất chiếu từ database
        model.addAttribute("showtimes", showtimeRepository.findAll());

        // Trả về file:
        // templates/admin/showtimes/list.html
        return "admin/showtimes/list";
    }

    // =========================
    // MỞ FORM THÊM SUẤT CHIẾU
    // =========================
    @GetMapping("/create")
    public String createForm(Model model) {

        // Tạo object Showtime rỗng
        Showtime showtime = new Showtime();

        // Tránh lỗi NullPointerException khi binding:
        // th:field="*{movie.id}"
        showtime.setMovie(new Movie());

        // Tránh lỗi NullPointerException khi binding:
        // th:field="*{room.id}"
        showtime.setRoom(new Room());

        // Gửi object showtime sang form
        model.addAttribute("showtime", showtime);

        // Gửi danh sách phim sang form
        model.addAttribute("movies", movieRepository.findAll());

        // Gửi danh sách phòng sang form
        model.addAttribute("rooms", roomRepository.findAll());

        // Trả về file form.html
        return "admin/showtimes/form";
    }

    // =========================
    // LƯU SUẤT CHIẾU
    // =========================
    @PostMapping("/save")
    public String saveShowtime(

            // Validate dữ liệu từ form
            @Valid @ModelAttribute("showtime") Showtime showtime,

            // Chứa lỗi validate
            BindingResult bindingResult,

            Model model,

            // Dùng để gửi thông báo redirect
            RedirectAttributes redirectAttributes
    ) {

        // =========================
        // KIỂM TRA VALIDATE
        // =========================
        if (bindingResult.hasErrors()) {

            // Nếu có lỗi thì load lại dữ liệu dropdown
            model.addAttribute("movies", movieRepository.findAll());
            model.addAttribute("rooms", roomRepository.findAll());

            // Quay lại form
            return "admin/showtimes/form";
        }

        // =========================
        // LẤY THÔNG TIN PHIM
        // =========================

        // Tìm phim theo id
        Movie movie = movieRepository
                .findById(showtime.getMovie().getId())
                .orElseThrow();

        // =========================
        // TÍNH GIỜ KẾT THÚC
        // =========================

        // Giờ bắt đầu
        LocalDateTime newStart = showtime.getStartTime();

        // Giờ kết thúc:
        // giờ bắt đầu + thời lượng phim + 15 phút dọn phòng
        LocalDateTime newEnd =
                newStart
                        .plusMinutes(movie.getDuration())
                        .plusMinutes(15);

        // Lưu endTime vào showtime
        showtime.setEndTime(newEnd);

        // =========================
        // LẤY KHOẢNG THỜI GIAN TRONG NGÀY
        // =========================

        // Đầu ngày
        LocalDateTime startOfDay =
                newStart.toLocalDate().atStartOfDay();

        // Cuối ngày
        LocalDateTime endOfDay =
                startOfDay.plusDays(1).minusSeconds(1);

        // =========================
        // LẤY CÁC SUẤT CHIẾU CÙNG PHÒNG
        // =========================

        List<Showtime> existingShowtimes =
                showtimeRepository.findByRoomIdAndDateRange(
                        showtime.getRoom().getId(),
                        startOfDay,
                        endOfDay
                );

        // =========================
        // KIỂM TRA TRÙNG LỊCH
        // =========================

        for (Showtime existing : existingShowtimes) {

            // Nếu là chính nó khi edit thì bỏ qua
            if (existing.getId() != null &&
                    existing.getId().equals(showtime.getId())) {
                continue;
            }

            // Giờ bắt đầu của suất chiếu cũ
            LocalDateTime existStart = existing.getStartTime();

            // Giờ kết thúc của suất chiếu cũ
            LocalDateTime existEnd;

            // Nếu đã có endTime thì dùng
            if (existing.getEndTime() != null) {

                existEnd = existing.getEndTime();

            } else {

                // Nếu chưa có thì tự tính
                existEnd =
                        existStart
                                .plusMinutes(existing.getMovie().getDuration())
                                .plusMinutes(15);
            }

            // =========================
            // ĐIỀU KIỆN TRÙNG LỊCH
            // =========================
            /*
                newStart < existEnd
                &&
                newEnd > existStart

                => nghĩa là 2 khoảng thời gian bị giao nhau
             */

            if (newStart.isBefore(existEnd)
                    && newEnd.isAfter(existStart)) {

                // Thông báo lỗi
                redirectAttributes.addFlashAttribute(
                        "error",
                        "Lịch chiếu trùng với phim: "
                                + existing.getMovie().getTitle()
                                + " (" + existStart + ")"
                );

                // Redirect về form tạo
                return "redirect:/admin/showtimes/create";
            }
        }

        // =========================
        // LƯU DATABASE
        // =========================

        showtimeRepository.save(showtime);

        // Thông báo thành công
        redirectAttributes.addFlashAttribute(
                "success",
                "Thêm suất chiếu thành công!"
        );

        // Quay về danh sách suất chiếu
        return "redirect:/admin/showtimes";
    }
}