package com.headshot.gateway.auth;

import com.headshot.gateway.user.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security configuration for the UnFumble Gateway.
 *
 * Public endpoints:
 *   GET  /health
 *   POST /api/v1/auth/register
 *   POST /api/v1/auth/login
 *
 * Everything else requires authentication.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * BCrypt is the standard choice — cost factor 12 (default) is a good balance
     * between security and registration latency on modern hardware.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * AuthenticationManager backed by our UserService + BCrypt encoder.
     * Exposed as a bean so AuthController can inject it directly.
     */
    @Bean
    public AuthenticationManager authenticationManager(UserService userService,
                                                       PasswordEncoder passwordEncoder) {
        // Spring Security 7 (Spring Boot 4.x): UserDetailsService is a constructor arg
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF for a stateless REST API (enable when adding CSRF-sensitive UIs)
            .csrf(AbstractHttpConfigurer::disable)

            .authorizeHttpRequests(auth -> auth
                // Public endpoints — no token required
                .requestMatchers("/health").permitAll()
                .requestMatchers("/api/v1/auth/**").permitAll()
                // Everything else must be authenticated
                .anyRequest().authenticated()
            )

            // No form login — this is a pure REST/JSON API
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }
}
