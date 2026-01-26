package com.pit.web.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// Application component.
public record LoginRequest(
        @Email @NotBlank String email,
        @NotBlank String password
) {}
