package com.pit.service;

import com.pit.domain.Role;
import com.pit.domain.User;
import com.pit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.pit.security.JwtService;

import java.util.Optional;

// Authentication and user helper service.
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final JwtService jwt;

    // Handles register user request operation
    public User registerUser(String email, String rawPassword) {
        if (users.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email déjà utilisé");
        }
        User u = new User();
        u.setEmail(email);
        u.setPassword(encoder.encode(rawPassword));
        u.setRole(Role.USER);
        return users.save(u);
    }

    // Handles register request operation
    public String register(String email, String rawPassword) {
        User saved = registerUser(email, rawPassword);
        return jwt.generateToken(org.springframework.security.core.userdetails.User
                .withUsername(saved.getEmail())
                .password(saved.getPassword())
                .roles(saved.getRole().name())
                .build());
    }

    // Handles authenticate request operation
    public Authentication authenticate(String email, String rawPassword) {
        return authManager.authenticate(new UsernamePasswordAuthenticationToken(email, rawPassword));
    }

    // Handles login request operation
    public String login(String email, String rawPassword) {
        Authentication auth = authenticate(email, rawPassword);
        return jwt.generateToken((org.springframework.security.core.userdetails.User) auth.getPrincipal());
    }

    // Handles get current user request operation
    public Optional<User> getCurrentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            return Optional.empty();
        }
        return users.findByEmail(auth.getName());
    }

    // Handles is current user admin request operation
    public boolean isCurrentUserAdmin() {
        return getCurrentUser()
                .map(user -> user.getRole() == Role.ADMIN)
                .orElse(false);
    }

    /** Returns the database identifier of the current user if present. */
    // Handles get current user id request operation
    public Long getCurrentUserId() {
        return getCurrentUser().map(User::getId).orElse(null);
    }
}
