package com.duyanhnguyen.petworld.backend.configuration;

import com.duyanhnguyen.petworld.backend.entity.UserEntity;
import com.duyanhnguyen.petworld.backend.enums.Role;
import com.duyanhnguyen.petworld.backend.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
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
    @Value("${admin.username}")
    String adminUsername;

    @NonFinal
    @Value("${admin.email}")
    String adminEmail;

    @NonFinal
    @Value("${admin.password}")
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
