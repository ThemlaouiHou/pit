package com.pit.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = "test-secret-0123456789ABCDEF-0123456789ABCDEF";

    @Test
    void generateTokenAndExtractUsername() {
        JwtService jwtService = new JwtService(SECRET, 3600000);
        var user = User.withUsername("user@test.local")
                .password("pw")
                .roles("USER")
                .build();

        String token = jwtService.generateToken(user);

        assertThat(jwtService.extractUsername(token)).isEqualTo("user@test.local");
    }

    @Test
    void tokenContainsRoleClaim() {
        JwtService jwtService = new JwtService(SECRET, 3600000);
        var user = User.withUsername("admin@test.local")
                .password("pw")
                .roles("ADMIN")
                .build();

        String token = jwtService.generateToken(user);

        String role = jwtService.extractClaim(token, claims -> (String) claims.get("role"));
        assertThat(role).isEqualTo("ROLE_ADMIN");
    }

    @Test
    void expiredTokenIsInvalid() {
        JwtService jwtService = new JwtService(SECRET, -1000);
        var user = User.withUsername("user@test.local")
                .password("pw")
                .roles("USER")
                .build();

        String token = jwtService.generateToken(user);

        assertThatThrownBy(() -> jwtService.isTokenValid(token, user))
                .isInstanceOf(io.jsonwebtoken.ExpiredJwtException.class);
    }
}
