package com.unfumble.gateway.auth;

import com.unfumble.gateway.user.User;
import com.unfumble.gateway.user.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Handles user registration and login.
 *
 * Auth strategy: simple session placeholder — a proper JWT implementation
 * will replace this once Spring Security OAuth2 resource server is added.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    public AuthController(UserService userService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    // ── Register ─────────────────────────────────────────────────────────────

    /**
     * POST /api/v1/auth/register
     * Creates a new user account.
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = userService.registerUser(request.email(), request.password());
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "User registered successfully",
                    "email", user.getEmail()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    /**
     * POST /api/v1/auth/login
     * Validates credentials and returns a placeholder session token.
     *
     * TODO: replace with a signed JWT once JWT support is added.
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
            // Placeholder — real token generation comes with JWT step
            String placeholderToken = "session-" + auth.getName().hashCode();
            return ResponseEntity.ok(Map.of(
                    "token", placeholderToken,
                    "note", "placeholder token — JWT coming soon"
            ));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "error", "Invalid email or password"
            ));
        }
    }

    // ── Request DTOs ──────────────────────────────────────────────────────────

    public record RegisterRequest(
            @Email @NotBlank String email,
            @NotBlank @Size(min = 8) String password
    ) {}

    public record LoginRequest(
            @Email @NotBlank String email,
            @NotBlank String password
    ) {}
}
