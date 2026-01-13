package com.pit.web;

import com.pit.service.AuthService;
import com.pit.security.JwtService;
import com.pit.web.dto.auth.LoginRequest;
import com.pit.web.dto.auth.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = com.pit.web.controller.AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean AuthService authService;
    @MockBean JwtService jwtService;
    @MockBean UserDetailsService userDetailsService;

    @Test
    void registerReturnsToken() throws Exception {
        when(authService.register("user@test.local", "Secret123"))
                .thenReturn("token-123");

        var payload = objectMapper.writeValueAsString(
                new RegisterRequest("user@test.local", "Secret123")
        );

        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-123"));
    }

    @Test
    void loginReturnsToken() throws Exception {
        when(authService.login("user@test.local", "Secret123"))
                .thenReturn("token-abc");

        var payload = objectMapper.writeValueAsString(
                new LoginRequest("user@test.local", "Secret123")
        );

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-abc"));
    }

    @Test
    void registerRejectsInvalidEmail() throws Exception {
        var payload = objectMapper.writeValueAsString(
                new RegisterRequest("not-an-email", "Secret123")
        );

        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginRejectsBlankPassword() throws Exception {
        var payload = objectMapper.writeValueAsString(
                new LoginRequest("user@test.local", "")
        );

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }
}
