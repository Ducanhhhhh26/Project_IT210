package controller;
import jakarta.validation.Valid;
import model.dto.ProfileUpdateForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.core.Authentication;
import repository.UserRepository;
import model.entity.User;
import model.entity.Role;
import service.BookingService;
import model.entity.BookingStatus;
import model.entity.Booking;
import repository.BookingRepository;
import repository.GenreRepository;
import repository.MovieRepository;
import repository.ShowtimeRepository;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Controller
public class MainController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private ShowtimeRepository showtimeRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @GetMapping("/")
    public String home(Model model) {
        List<model.entity.Movie> movies = movieRepository.findAll();
        Map<Long, Long> upcomingShowtimeCountByMovie = new HashMap<>();
        for (Object[] row : showtimeRepository.countUpcomingShowtimesByMovie(LocalDateTime.now())) {
            Long movieId = (Long) row[0];
            Long count = (Long) row[1];
            upcomingShowtimeCountByMovie.put(movieId, count);
        }

        model.addAttribute("movies", movies);
        model.addAttribute("upcomingShowtimeCountByMovie", upcomingShowtimeCountByMovie);
        model.addAttribute("genres", genreRepository.findAll());
        return "index";
    }
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @PostMapping("/register")
    public String processRegister(@RequestParam String email, @RequestParam String phone, @RequestParam String username, @RequestParam String password, Model model) {
        if(userRepository.findByEmail(email) != null) {
            model.addAttribute("error", "Email đã tồn tại!");
            return "register";
        }
        if(userRepository.findByUsername(username) != null) {
            model.addAttribute("error", "Tên đăng nhập đã tồn tại!");
            return "register";
        }
        User user = new User();
        user.setEmail(email);
        user.setPhone(phone);
        user.setUsername(username);
        user.setFullName(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(Role.CUSTOMER);
        userRepository.save(user);
        return "redirect:/login?registered=true";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();
        BigDecimal confirmedRevenue = bookingRepository.sumTotalPriceByStatus(BookingStatus.CONFIRMED);

        model.addAttribute("movieCount", movieRepository.count());
        model.addAttribute("todayShowtimeCount", showtimeRepository.countByStartTimeBetween(startOfDay, endOfDay));
        model.addAttribute("confirmedRevenue", formatVnd(confirmedRevenue));
        return "admin/dashboard";
    }

    @GetMapping("/staff/dashboard")
    public String staffDashboard(@RequestParam(required = false) String bookingCode, Model model) {
        model.addAttribute("recentBookings", bookingService.getRecentBookingsForStaff(10));

        if (bookingCode != null && !bookingCode.trim().isEmpty()) {
            model.addAttribute("searchedCode", bookingCode.trim());
            try {
                Booking foundBooking = bookingService.getBookingForStaffByCode(bookingCode);
                model.addAttribute("foundBooking", foundBooking);
                model.addAttribute("msgSuccess", "Da tim thay don hang.");
            } catch (Exception e) {
                model.addAttribute("msgError", e.getMessage());
            }
        }

        return "staff/dashboard";
    }

    private String formatVnd(BigDecimal amount) {
        BigDecimal safeAmount = amount == null ? BigDecimal.ZERO : amount;
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        formatter.setMaximumFractionDigits(0);
        return formatter.format(safeAmount) + " VND";
    }

    @GetMapping("/profile")
    public String customerProfile(Model model, Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName());
        model.addAttribute("bookings", bookingService.getBookingHistory(user.getId()));
        model.addAttribute("user", user);
        if (!model.containsAttribute("profileForm")) {
            ProfileUpdateForm profileForm = new ProfileUpdateForm();
            profileForm.setFullName(user.getFullName());
            profileForm.setPhone(user.getPhone());
            model.addAttribute("profileForm", profileForm);
        }
        return "profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@Valid @ModelAttribute("profileForm") ProfileUpdateForm profileForm,
                                BindingResult bindingResult,
                                Authentication authentication,
                                Model model) {
        User user = userRepository.findByEmail(authentication.getName());
        if (bindingResult.hasErrors()) {
            model.addAttribute("bookings", bookingService.getBookingHistory(user.getId()));
            model.addAttribute("user", user);
            model.addAttribute("msgError", "Vui lòng kiểm tra lại thông tin.");
            return "profile";
        }

        user.setFullName(profileForm.getFullName().trim());
        String normalizedPhone = profileForm.getPhone() == null ? "" : profileForm.getPhone().trim();
        user.setPhone(normalizedPhone.isEmpty() ? null : normalizedPhone);
        userRepository.save(user);

        model.addAttribute("bookings", bookingService.getBookingHistory(user.getId()));
        model.addAttribute("user", user);
        model.addAttribute("msgSuccess", "Cập nhật hồ sơ thành công.");
        model.addAttribute("profileForm", profileForm);
        return "profile";
    }

    @PostMapping("/booking/cancel")
    public String cancelBooking(@RequestParam Long bookingId, Authentication authentication, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        User user = userRepository.findByEmail(authentication.getName());
        try {
            bookingService.cancelBooking(bookingId, user.getId());
            redirectAttributes.addFlashAttribute("msgSuccess", "Đã hủy vé thành công và hoàn trả ghế!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("msgError", e.getMessage());
        }
        return "redirect:/profile";
    }
}
