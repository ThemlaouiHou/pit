package com.pit.service;

import com.pit.domain.Role;
import com.pit.domain.User;
import com.pit.repository.UserRepository;
import com.pit.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository users;
    @Mock PasswordEncoder encoder;
    @Mock AuthenticationManager authManager;
    @Mock JwtService jwt;
    @InjectMocks AuthService authService;

    @Test
    void registerUserRejectsDuplicateEmail() {
        when(users.findByEmail("dup@test.local")).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> authService.registerUser("dup@test.local", "pwd"))
                .isInstanceOf(IllegalArgumentException.class);
        verify(users, never()).save(any());
    }

    @Test
    void registerUserEncodesPasswordAndSetsRole() {
        when(users.findByEmail("user@test.local")).thenReturn(Optional.empty());
        when(encoder.encode("pwd")).thenReturn("hashed");
        when(users.save(any())).thenAnswer(inv -> inv.getArgument(0));

        User saved = authService.registerUser("user@test.local", "pwd");

        assertThat(saved.getEmail()).isEqualTo("user@test.local");
        assertThat(saved.getPassword()).isEqualTo("hashed");
        assertThat(saved.getRole()).isEqualTo(Role.USER);
    }

    @Test
    void registerReturnsJwtToken() {
        when(users.findByEmail("user@test.local")).thenReturn(Optional.empty());
        when(encoder.encode("pwd")).thenReturn("hashed");
        when(users.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(jwt.generateToken(any(UserDetails.class))).thenReturn("token-xyz");

        String token = authService.register("user@test.local", "pwd");

        assertThat(token).isEqualTo("token-xyz");
        verify(jwt).generateToken(any(UserDetails.class));
    }

    @Test
    void loginUsesAuthenticationManagerAndReturnsToken() {
        Authentication authentication = mock(Authentication.class);
        UserDetails principal = org.springframework.security.core.userdetails.User
                .withUsername("user@test.local")
                .password("hashed")
                .roles("USER")
                .build();

        when(authManager.authenticate(any())).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(jwt.generateToken(principal)).thenReturn("token-login");

        String token = authService.login("user@test.local", "pwd");

        assertThat(token).isEqualTo("token-login");
        verify(authManager).authenticate(any());
    }
}
