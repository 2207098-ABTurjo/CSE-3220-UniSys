package com.university.ums.config;

import com.university.ums.model.AppUser;
import com.university.ums.model.Role;
import com.university.ums.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner createDefaultAdmin(UserRepository repository, PasswordEncoder encoder) {
        return args -> {
            if (!repository.existsByUsername("admin")) {
                AppUser admin = new AppUser();
                admin.setUsername("admin");
                admin.setPassword(encoder.encode("admin123"));
                admin.setFullName("System Administrator");
                admin.setEmail("admin@university.local");
                admin.setRole(Role.ADMIN);
                repository.save(admin);
            }
        };
    }
}
