package com.app.auth.config;

import com.app.auth.entity.Role;
import com.app.auth.entity.User;
import com.app.auth.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

/**
 * A small, explicit home for miscellaneous @Bean definitions that don't
 * belong to any single feature (the opposite of scattering @Bean methods
 * randomly across unrelated classes).
 *
 * @Profile("dev") shows Spring's profile-based bean swapping: this seed-data
 * runner only activates when the "dev" profile is active (see
 * application.yml -> spring.profiles.active), never in "prod".
 */
@Configuration
public class AppBeanConfig {

    @Bean
    @Profile("dev")
    public CommandLineRunner seedAdminUser(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            userRepository.findByUsername("admin").ifPresentOrElse(admin -> {
                // Truncating user_roles leaves the user with no authorities.
                if (admin.getRoles() == null || admin.getRoles().isEmpty()
                        || !admin.getRoles().contains(Role.ROLE_ADMIN)) {
                    admin.setRoles(Set.of(Role.ROLE_ADMIN, Role.ROLE_USER));
                    admin.setEnabled(true);
                    userRepository.save(admin);
                }
            }, () -> userRepository.save(User.builder()
                    .username("admin")
                    .email("admin@example.com")
                    .password(passwordEncoder.encode("admin123"))
                    .roles(Set.of(Role.ROLE_ADMIN, Role.ROLE_USER))
                    .enabled(true)
                    .build()));
        };
    }
}
