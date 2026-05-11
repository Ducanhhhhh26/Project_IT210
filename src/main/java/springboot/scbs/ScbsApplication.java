package springboot.scbs;

import model.entity.Genre;
import model.entity.Movie;
import model.entity.Role;
import model.entity.Room;
import model.entity.Seat;
import model.entity.User;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.crypto.password.PasswordEncoder;
import repository.GenreRepository;
import repository.MovieRepository;
import repository.RoomRepository;
import repository.SeatRepository;
import repository.UserRepository;

import java.time.LocalDate;

@SpringBootApplication
@EntityScan(basePackages = {"model.entity"})
@EnableJpaRepositories(basePackages = {"repository"})
@ComponentScan(basePackages = {"springboot.scbs", "config", "controller", "service"})
public class ScbsApplication {
    public static void main(String[] args) {
        SpringApplication.run(ScbsApplication.class, args);
    }

    @Bean
    public CommandLineRunner initData(UserRepository userRepository,
                                      PasswordEncoder passwordEncoder,
                                      GenreRepository genreRepository,
                                      RoomRepository roomRepository,
                                      MovieRepository movieRepository,
                                      SeatRepository seatRepository) {
        return args -> {
            User admin = userRepository.findByUsername("admin");
            if (admin == null) {
                admin = new User();
                admin.setUsername("admin");
                admin.setFullName("Administrator");
                admin.setEmail("admin@gmail.com");
                admin.setPassword(passwordEncoder.encode("123456"));
                admin.setRole(Role.ADMIN);
                userRepository.save(admin);
            } else {
                boolean adminChanged = false;
                if (admin.getFullName() == null || admin.getFullName().isEmpty()) {
                    admin.setFullName("Administrator");
                    adminChanged = true;
                }
                if (admin.getEmail() == null || admin.getEmail().isEmpty() || !"admin@gmail.com".equalsIgnoreCase(admin.getEmail())) {
                    admin.setEmail("admin@gmail.com");
                    adminChanged = true;
                }
                if (admin.getRole() != Role.ADMIN) {
                    admin.setRole(Role.ADMIN);
                    adminChanged = true;
                }
                if (admin.getPassword() == null || !passwordEncoder.matches("123456", admin.getPassword())) {
                    admin.setPassword(passwordEncoder.encode("123456"));
                    adminChanged = true;
                }
                if (adminChanged) {
                    userRepository.save(admin);
                }
            }

            User staff = userRepository.findByUsername("staff");
            if (staff == null) {
                staff = new User();
                staff.setUsername("staff");
                staff.setFullName("Staff Account");
                staff.setEmail("staff@gmail.com");
                staff.setPassword(passwordEncoder.encode("123456"));
                staff.setRole(Role.STAFF);
                userRepository.save(staff);
            } else {
                boolean staffChanged = false;
                if (staff.getFullName() == null || staff.getFullName().isEmpty()) {
                    staff.setFullName("Staff Account");
                    staffChanged = true;
                }
                if (staff.getEmail() == null || staff.getEmail().isEmpty() || !"staff@gmail.com".equalsIgnoreCase(staff.getEmail())) {
                    staff.setEmail("staff@gmail.com");
                    staffChanged = true;
                }
                if (staff.getRole() != Role.STAFF) {
                    staff.setRole(Role.STAFF);
                    staffChanged = true;
                }
                if (staff.getPassword() == null || !passwordEncoder.matches("123456", staff.getPassword())) {
                    staff.setPassword(passwordEncoder.encode("123456"));
                    staffChanged = true;
                }
                if (staffChanged) {
                    userRepository.save(staff);
                }
            }

            User customer = userRepository.findByUsername("customer");
            if (customer == null) {
                customer = new User();
                customer.setUsername("customer");
                customer.setFullName("Customer Account");
                customer.setEmail("customer@gmail.com");
                customer.setPassword(passwordEncoder.encode("123456"));
                customer.setRole(Role.CUSTOMER);
                userRepository.save(customer);
            } else {
                boolean customerChanged = false;
                if (customer.getFullName() == null || customer.getFullName().isEmpty()) {
                    customer.setFullName("Customer Account");
                    customerChanged = true;
                }
                if (customer.getEmail() == null || customer.getEmail().isEmpty() || !"customer@gmail.com".equalsIgnoreCase(customer.getEmail())) {
                    customer.setEmail("customer@gmail.com");
                    customerChanged = true;
                }
                if (customer.getRole() != Role.CUSTOMER) {
                    customer.setRole(Role.CUSTOMER);
                    customerChanged = true;
                }
                if (customer.getPassword() == null || !passwordEncoder.matches("123456", customer.getPassword())) {
                    customer.setPassword(passwordEncoder.encode("123456"));
                    customerChanged = true;
                }
                if (customerChanged) {
                    userRepository.save(customer);
                }
            }

            Genre action = getOrCreateGenre(genreRepository, "Hành động", "Hanh dong");
            Genre comedy = getOrCreateGenre(genreRepository, "Hài hước", "Hai huoc");
            Genre romance = getOrCreateGenre(genreRepository, "Tình cảm", "Tinh cam");
            Genre horror = getOrCreateGenre(genreRepository, "Kinh dị", "Kinh di");
            Genre sciFi = getOrCreateGenre(genreRepository, "Viễn tưởng", "Vien tuong");
            Genre adventure = getOrCreateGenre(genreRepository, "Phiêu lưu", "Phieu luu");
            Genre animation = getOrCreateGenre(genreRepository, "Hoạt hình", "Hoat hinh");

            if (roomRepository.count() == 0) {
                Room r1 = new Room(); r1.setName("Phong 01"); r1.setCapacity(50); roomRepository.save(r1);
                Room r2 = new Room(); r2.setName("Phong 02 IMAX"); r2.setCapacity(80); roomRepository.save(r2);
                Room r3 = new Room(); r3.setName("Phong 03 3D"); r3.setCapacity(40); roomRepository.save(r3);
            }

            if (seatRepository.count() == 0) {
                for (Room room : roomRepository.findAll()) {
                    for (int i = 1; i <= room.getCapacity(); i++) {
                        String seatName = toSeatName(i);
                        Seat seat = new Seat();
                        seat.setRoom(room);
                        seat.setSeatName(seatName);
                        seat.setRowName(String.valueOf(seatName.charAt(0)));
                        seatRepository.save(seat);
                    }
                }
            }

            seedMovie(movieRepository, "Ke Phan Dien Cuoi Cung", "Mot ke phan dien tim cach viet lai so phan cua minh trong the gioi dien anh.", 126, LocalDate.of(2025, 12, 20), action, "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?auto=format&fit=crop&w=600&h=900&q=85");
            seedMovie(movieRepository, "Vu Tru Bong Toi", "Chuyen du hanh xuyen khong gian noi mot phi hanh doan doi mat bi mat ngoai hanh tinh.", 142, LocalDate.of(2026, 1, 10), sciFi, "https://images.unsplash.com/photo-1446776811953-b23d57bd21aa?auto=format&fit=crop&w=600&h=900&q=85");
            seedMovie(movieRepository, "Mua He Vinh Cuu", "Cau chuyen tinh cam diu dang giua hai nguoi tre gap nhau trong mot mua he kho quen.", 98, LocalDate.of(2025, 9, 5), romance, "https://images.unsplash.com/photo-1529518969858-8baa65152fc8?auto=format&fit=crop&w=600&h=900&q=85");
            seedMovie(movieRepository, "Bong Toi Thuc Day", "Mot can nha cu bi danh thuc boi nhung bi mat kinh hoang trong dem mua.", 105, LocalDate.of(2025, 10, 31), horror, "https://images.unsplash.com/photo-1509248961158-e54f6934749c?auto=format&fit=crop&w=600&h=900&q=85");
            seedMovie(movieRepository, "Vuong Quoc Phep Thuat", "Cuoc phieu luu cua mot co be lac vao vuong quoc phep thuat day sac mau.", 110, LocalDate.of(2025, 11, 15), animation, "https://images.unsplash.com/photo-1518709268805-4e9042af2176?auto=format&fit=crop&w=600&h=900&q=85");
            seedMovie(movieRepository, "Chuyen Tau Nua Dem", "Mot chuyen tau bi an dua hanh khach den nhung su that bi che giau.", 118, LocalDate.of(2026, 2, 2), adventure, "https://images.unsplash.com/photo-1474487548417-781cb71495f3?auto=format&fit=crop&w=600&h=900&q=85");
            seedMovie(movieRepository, "Doi Dac Nhiem Do", "Biet doi dac nhiem thuc hien phi vu giai cuu con tin trong thoi gian ngan.", 132, LocalDate.of(2025, 8, 18), action, "https://images.unsplash.com/photo-1515859005217-8a1f08870f59?auto=format&fit=crop&w=600&h=900&q=85");
            seedMovie(movieRepository, "Ngay Cuoi Tuan Roi Ren", "Nhom ban than vo tinh bien mot ngay nghi thanh chuoi tinh huong hai huoc.", 94, LocalDate.of(2025, 7, 12), comedy, "https://images.unsplash.com/photo-1527224538127-2104bb71c51b?auto=format&fit=crop&w=600&h=900&q=85");
            seedMovie(movieRepository, "Thanh Pho Sau Man Mua", "Mot phong vien dieu tra vu an lon an sau nhung con pho mua dem.", 121, LocalDate.of(2026, 3, 8), action, "https://images.unsplash.com/photo-1494526585095-c41746248156?auto=format&fit=crop&w=600&h=900&q=85");
            seedMovie(movieRepository, "Han Tinh Tu Sao Hoa", "Mot nha khoa hoc trai dat va tin hieu tu Sao Hoa tao nen moi lien ket vuot khong gian.", 116, LocalDate.of(2026, 4, 22), sciFi, "https://images.unsplash.com/photo-1614728894747-a83421e2b9c9?auto=format&fit=crop&w=600&h=900&q=85");
        };
    }

    private Genre getOrCreateGenre(GenreRepository genreRepository, String displayName, String legacyName) {
        Genre genre = genreRepository.findByName(displayName);
        if (genre != null) {
            return genre;
        }

        Genre legacyGenre = genreRepository.findByName(legacyName);
        if (legacyGenre != null) {
            legacyGenre.setName(displayName);
            return genreRepository.save(legacyGenre);
        }

        genre = new Genre();
        genre.setName(displayName);
        return genreRepository.save(genre);
    }

    private void seedMovie(MovieRepository movieRepository,
                           String title,
                           String description,
                           int duration,
                           LocalDate releaseDate,
                           Genre genre,
                           String posterUrl) {
        if (movieRepository.existsByTitle(title)) {
            return;
        }

        Movie movie = new Movie();
        movie.setTitle(title);
        movie.setDescription(description);
        movie.setDuration(duration);
        movie.setReleaseDate(releaseDate);
        movie.setGenre(genre);
        movie.setPosterUrl(posterUrl);
        movieRepository.save(movie);
    }

    private String toSeatName(int index) {
        int zeroBased = index - 1;
        char row = (char) ('A' + (zeroBased / 10));
        int number = (zeroBased % 10) + 1;
        return row + String.valueOf(number);
    }
}
