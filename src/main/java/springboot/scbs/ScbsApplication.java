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
import model.entity.User;
import model.entity.Role;

@SpringBootApplication
@EntityScan(basePackages = {"model.entity"})
@EnableJpaRepositories(basePackages = {"repository"})
@ComponentScan(basePackages = {"springboot.scbs", "config", "controller", "service"})
public class ScbsApplication {
    public static void main(String[] args) {
        SpringApplication.run(ScbsApplication.class, args);
    }

    @Bean
    public CommandLineRunner initData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
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
        };
    }
}
