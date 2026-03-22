package com.example.expensetracker.config;

import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class StartupDataLoader {

    @Bean
    public CommandLineRunner initDefaultAdmin(UserRepository userRepository,
                                              PasswordEncoder passwordEncoder,
                                              @Value("${ADMIN_NAME:System Admin}") String adminName,
                                              @Value("${ADMIN_EMAIL:admin@expensetracker.com}") String adminEmail,
                                              @Value("${ADMIN_PASSWORD:123456}") String adminPassword) {
        return args -> {
            userRepository.findByEmail(adminEmail)
                    .ifPresentOrElse(user -> {
                        if (user.getRole() != User.Role.ADMIN) {
                            user.setRole(User.Role.ADMIN);
                            userRepository.save(user);
                        }
                    }, () -> {
                        User admin = User.builder()
                                .fullName(adminName)
                                .email(adminEmail)
                                .password(passwordEncoder.encode(adminPassword))
                                .role(User.Role.ADMIN)
                                .build();
                        userRepository.save(admin);
                    });
        };
    }
}

