package springboot.scbs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.crypto.password.PasswordEncoder;
import repository.UserRepository;
import repository.GenreRepository;
import repository.RoomRepository;
import repository.MovieRepository;
import model.entity.User;
import model.entity.Role;
import model.entity.Genre;
import model.entity.Room;
import model.entity.Movie;
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
                                      MovieRepository movieRepository) {
        return args -> {
            User admin = userRepository.findByUsername("admin");
            if (admin == null) {
                admin = new User();
                admin.setUsername("admin");
                admin.setEmail("admin@gmail.com");
                admin.setPassword(passwordEncoder.encode("123456"));
                admin.setRole(Role.ADMIN);
                userRepository.save(admin);
            } else if (admin.getEmail() == null || admin.getEmail().isEmpty()) {
                admin.setEmail("admin@gmail.com");
                userRepository.save(admin);
            }

            User staff = userRepository.findByUsername("staff");
            if (staff == null) {
                staff = new User();
                staff.setUsername("staff");
                staff.setEmail("staff@gmail.com");
                staff.setPassword(passwordEncoder.encode("123456"));
                staff.setRole(Role.STAFF);
                userRepository.save(staff);
            } else if (staff.getEmail() == null || staff.getEmail().isEmpty()) {
                staff.setEmail("staff@gmail.com");
                userRepository.save(staff);
            }

            // CORE-04: Seed data cho Thể loại (Genre) và Phòng (Room)
            if (genreRepository.count() == 0) {
                Genre g1 = new Genre(); g1.setName("Hành động"); genreRepository.save(g1);
                Genre g2 = new Genre(); g2.setName("Hài hước"); genreRepository.save(g2);
                Genre g3 = new Genre(); g3.setName("Tình cảm"); genreRepository.save(g3);
                Genre g4 = new Genre(); g4.setName("Kinh dị"); genreRepository.save(g4);
                Genre g5 = new Genre(); g5.setName("Viễn tưởng"); genreRepository.save(g5);
            }

            if (roomRepository.count() == 0) {
                Room r1 = new Room(); r1.setName("Phòng 01"); r1.setCapacity(50); roomRepository.save(r1);
                Room r2 = new Room(); r2.setName("Phòng 02 IMAX"); r2.setCapacity(80); roomRepository.save(r2);
                Room r3 = new Room(); r3.setName("Phòng 03 3D"); r3.setCapacity(40); roomRepository.save(r3);
            }

            // Seed thêm 1 bộ phim mẫu để trang chủ không bị trống
            if (movieRepository.count() == 0 && genreRepository.count() > 0) {
                Movie m1 = new Movie();
                m1.setTitle("Avenger: Hồi kết");
                m1.setDescription("Phim hành động siêu anh hùng vĩ đại nhất.");
                m1.setDuration(180);
                m1.setReleaseDate(LocalDate.now());
                m1.setGenre(genreRepository.findAll().get(0));
                movieRepository.save(m1);
            }
        };
    }
}
