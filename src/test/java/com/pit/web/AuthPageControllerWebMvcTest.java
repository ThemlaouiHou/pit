package com.pit.web;

import com.pit.service.AuthService;
import com.pit.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.csrf.DefaultCsrfToken;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(controllers = com.pit.web.controller.AuthPageController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthPageControllerWebMvcTest {

    @Autowired MockMvc mvc;
    @MockBean AuthService authService;
    @MockBean JwtService jwtService;
    @MockBean UserDetailsService userDetailsService;

    @Test
    void registerPageLoads() throws Exception {
        mvc.perform(get("/register").requestAttr("_csrf", csrfToken()))
                .andExpect(status().isOk())
                .andExpect(view().name("register"));
    }

    private static DefaultCsrfToken csrfToken() {
        return new DefaultCsrfToken("X-CSRF-TOKEN", "_csrf", "test");
    }
}
