package controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.core.Authentication;
import repository.UserRepository;
import model.entity.User;
import model.entity.Role;
import service.BookingService;
import model.entity.BookingStatus;
import repository.BookingRepository;
import repository.MovieRepository;
import repository.ShowtimeRepository;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;

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
    private ShowtimeRepository showtimeRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("movies", movieRepository.findAll());
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
    public String staffDashboard() {
        return "staff/dashboard";
    }

    private String formatVnd(BigDecimal amount) {
        BigDecimal safeAmount = amount == null ? BigDecimal.ZERO : amount;
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        return formatter.format(safeAmount) + " VND";
    }

    @GetMapping("/profile")
    public String customerProfile(Model model, Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName());
        model.addAttribute("bookings", bookingService.getBookingHistory(user.getId()));
        model.addAttribute("user", user);
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
