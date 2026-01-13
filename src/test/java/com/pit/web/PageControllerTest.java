package com.pit.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.pit.domain.Place;
import com.pit.domain.PlaceStatus;
import com.pit.security.JwtService;
import com.pit.service.AuthService;
import com.pit.service.PlaceService;
import com.pit.service.RatingService;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.csrf.DefaultCsrfToken;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(controllers = com.pit.web.controller.PageController.class)
@AutoConfigureMockMvc(addFilters = false)
class PageControllerTest {

    @Autowired MockMvc mvc;
    @MockBean PlaceService placeService;
    @MockBean RatingService ratingService;
    @MockBean AuthService authService;
    @MockBean JwtService jwtService;
    @MockBean UserDetailsService userDetailsService;

    @Test
    void homeLoads() throws Exception {
        when(placeService.findApproved(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));
        when(authService.getCurrentUser()).thenReturn(Optional.empty());
        mvc.perform(get("/").requestAttr("_csrf", csrfToken()))
                .andExpect(status().isOk());
    }

    @Test
    void loginLoads() throws Exception {
        mvc.perform(get("/login").requestAttr("_csrf", csrfToken()))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    void publicPlacePageLoadsWhenApproved() throws Exception {
        Place place = new Place();
        place.setId(1L);
        place.setName("Kasbah");
        place.setLat(30.0);
        place.setLng(-8.0);
        place.setStatus(PlaceStatus.APPROVED);

        when(placeService.findById(1L)).thenReturn(place);
        when(ratingService.findByPlace(anyLong(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));
        when(authService.getCurrentUser()).thenReturn(Optional.empty());
        when(authService.isCurrentUserAdmin()).thenReturn(false);

        mvc.perform(get("/places/1").requestAttr("_csrf", csrfToken()))
                .andExpect(status().isOk())
                .andExpect(view().name("places/detail"));
    }

    private static DefaultCsrfToken csrfToken() {
        return new DefaultCsrfToken("X-CSRF-TOKEN", "_csrf", "test");
    }
}
