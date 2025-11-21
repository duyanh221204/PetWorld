package org.example.backend.configuration;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.example.backend.entity.UserEntity;
import org.example.backend.enums.Role;
import org.example.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ApplicationInitConfig {

    UserRepository userRepository;
    PasswordEncoder passwordEncoder;

    @NonFinal
    @Value("${admin_username}")
    String adminUsername;

    @NonFinal
    @Value("${admin_email}")
    String adminEmail;

    @NonFinal
    @Value("${admin_password}")
    String adminPassword;

    @Bean
    public ApplicationRunner applicationRunner() {
        return args -> {
            if (!userRepository.existsByUsername(adminUsername)) {
                UserEntity admin = UserEntity.builder()
                        .username(adminUsername)
                        .email(adminEmail)
                        .hashedPassword(passwordEncoder.encode(adminPassword))
                        .role(Role.ADMIN)
                        .isActive(true)
                        .build();
                userRepository.save(admin);
            }
        };
    }

}
