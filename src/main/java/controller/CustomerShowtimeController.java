package controller;

import model.entity.Movie;
import model.entity.Booking;
import model.entity.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import repository.MovieRepository;
import repository.UserRepository;

import service.BookingService;

import java.util.List;

@Controller // Đánh dấu đây là Controller xử lý request từ client
public class CustomerShowtimeController {

    // =========================
    // REPOSITORY & SERVICE
    // =========================

    // Repository thao tác với bảng Movie
    @Autowired
    private MovieRepository movieRepository;

    // Service xử lý logic đặt vé
    @Autowired
    private BookingService bookingService;

    // Repository thao tác với bảng User
    @Autowired
    private UserRepository userRepository;

    // =====================================================
    // XEM DANH SÁCH SUẤT CHIẾU CỦA 1 PHIM
    // =====================================================

    @GetMapping("/movie/{id}/showtimes")
    public String viewShowtimes(
            @PathVariable Long id, // Lấy id từ URL
            Model model
    ) {

        // Tìm phim theo id
        Movie movie = movieRepository.findById(id)

                // Nếu không tìm thấy thì báo lỗi
                .orElseThrow(() ->
                        new IllegalArgumentException("Ma phim khong hop le"));

        // Gửi thông tin phim sang view
        model.addAttribute("movie", movie);

        // Lấy danh sách suất chiếu còn khả dụng
        model.addAttribute(
                "showtimeList",
                bookingService.getAvailableShowtimes(id)
        );

        // Trả về file:
        // templates/customer/showtimes.html
        return "customer/showtimes";
    }

    // =====================================================
    // XEM DANH SÁCH GHẾ CỦA SUẤT CHIẾU
    // =====================================================

    @GetMapping("/showtimes/{id}/seats")
    public String viewSeats(
            @PathVariable Long id,
            Model model
    ) throws Exception {

        // Lấy thông tin suất chiếu
        model.addAttribute(
                "showtime",
                bookingService.getShowtime(id)
        );

        // Lấy toàn bộ ghế của phòng
        model.addAttribute(
                "seats",
                bookingService.getSeatsForShowtime(id)
        );

        // Lấy danh sách ghế đã đặt
        model.addAttribute(
                "bookedSeatIds",
                bookingService.getBookedSeatIds(id)
        );

        // Trả về giao diện chọn ghế
        return "customer/seats";
    }

    // =====================================================
    // TẠO BOOKING (ĐẶT VÉ)
    // =====================================================

    @PostMapping("/booking/create")
    public String createBooking(

            // id suất chiếu
            @RequestParam Long showtimeId,

            // Danh sách ghế được chọn
            @RequestParam(required = false)
            List<Long> seatIds,

            // Lấy user đang login
            Authentication authentication,

            RedirectAttributes redirectAttributes
    ) {

        // Tìm user theo email login
        User user =
                userRepository.findByEmail(
                        authentication.getName()
                );

        try {

            // Tạo booking
            Booking booking =
                    bookingService.createBooking(
                            user,
                            showtimeId,
                            seatIds
                    );

            // Redirect sang trang thanh toán
            return "redirect:/booking/"
                    + booking.getId()
                    + "/payment";

        } catch (Exception e) {

            // Nếu lỗi thì gửi thông báo
            redirectAttributes.addFlashAttribute(
                    "msgError",
                    e.getMessage()
            );

            // Quay lại trang chọn ghế
            return "redirect:/showtimes/"
                    + showtimeId
                    + "/seats";
        }
    }

    // =====================================================
    // TRANG THANH TOÁN
    // =====================================================

    @GetMapping("/booking/{id}/payment")
    public String paymentPage(

            // id booking
            @PathVariable Long id,

            // User đang login
            Authentication authentication,

            Model model,

            RedirectAttributes redirectAttributes
    ) {

        // Lấy user theo email login
        User user =
                userRepository.findByEmail(
                        authentication.getName()
                );

        try {

            // Chỉ cho phép user xem booking của chính mình
            model.addAttribute(
                    "booking",
                    bookingService.getBookingForUser(
                            id,
                            user.getId()
                    )
            );

            // Trả về giao diện payment
            return "customer/payment";

        } catch (Exception e) {

            // Nếu lỗi thì hiện thông báo
            redirectAttributes.addFlashAttribute(
                    "msgError",
                    e.getMessage()
            );

            // Quay về profile
            return "redirect:/profile";
        }
    }

    // =====================================================
    // XÁC NHẬN THANH TOÁN
    // =====================================================

    @PostMapping("/booking/{id}/payment/confirm")
    public String confirmPayment(

            // id booking
            @PathVariable Long id,

            // User đang login
            Authentication authentication,

            RedirectAttributes redirectAttributes
    ) {

        // Lấy user đang login
        User user =
                userRepository.findByEmail(
                        authentication.getName()
                );

        try {

            // Xử lý thanh toán
            bookingService.confirmPayment(
                    id,
                    user.getId()
            );

            // Hiện popup/thông báo thành công
            redirectAttributes.addFlashAttribute(
                    "showPaymentSuccess",
                    id
            );

            // Thông báo thành công
            redirectAttributes.addFlashAttribute(
                    "msgSuccess",
                    "Thanh toán thành công. Đang xử lý biên lai kết quả..."
            );

        } catch (Exception e) {

            // Nếu lỗi thì báo lỗi
            redirectAttributes.addFlashAttribute(
                    "msgError",
                    e.getMessage()
            );

            // Quay lại trang payment
            return "redirect:/booking/"
                    + id
                    + "/payment";
        }

        // Reload lại trang payment
        return "redirect:/booking/"
                + id
                + "/payment";
    }
}