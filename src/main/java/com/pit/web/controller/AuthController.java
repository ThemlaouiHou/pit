package com.pit.web.controller;

import com.pit.service.AuthService;
import com.pit.web.dto.auth.LoginRequest;
import com.pit.web.dto.auth.RegisterRequest;
import com.pit.web.dto.auth.AuthResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// REST endpoints for authentication.
@Tag(name = "auth-controller")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService auth;

    @PostMapping("/register")
    // Handles register request operation
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        String token = auth.register(req.email(), req.password());
        return ResponseEntity.ok(new AuthResponse(token));
    }

    @PostMapping("/login")
    // Handles login request operation
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        String token = auth.login(req.email(), req.password());
        return ResponseEntity.ok(new AuthResponse(token));
    }
}
