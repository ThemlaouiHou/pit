package com.pit.web;

import com.pit.domain.Place;
import com.pit.domain.PlaceStatus;
import com.pit.domain.Rating;
import com.pit.domain.User;
import com.pit.security.JwtService;
import com.pit.service.AuthService;
import com.pit.service.PlaceService;
import com.pit.service.RatingService;
import com.pit.web.dto.RatingDto;
import com.pit.web.mapper.RatingMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = com.pit.web.controller.RatingController.class)
@AutoConfigureMockMvc(addFilters = false)
class RatingControllerWebMvcTest {

    @Autowired MockMvc mvc;
    @MockBean RatingService ratingService;
    @MockBean PlaceService placeService;
    @MockBean AuthService authService;
    @MockBean RatingMapper ratingMapper;
    @MockBean JwtService jwtService;
    @MockBean UserDetailsService userDetailsService;

    @Test
    void listReturnsNotFoundWhenPlaceMissing() throws Exception {
        when(placeService.findById(99L))
                .thenThrow(new IllegalArgumentException("Place not found: 99"));

        mvc.perform(get("/api/places/99/ratings"))
                .andExpect(status().isNotFound());
    }

    @Test
    void listReturnsRatingsForApprovedPlace() throws Exception {
        Place place = new Place();
        place.setId(1L);
        place.setStatus(PlaceStatus.APPROVED);

        User user = new User();
        user.setId(2L);

        Rating rating = new Rating();
        rating.setId(3L);
        rating.setPlace(place);
        rating.setUser(user);
        rating.setScore(4);
        rating.setComment("Super");

        RatingDto dto = new RatingDto(3L, 1L, 2L, 4, "Super", Instant.parse("2026-01-01T00:00:00Z"));

        when(placeService.findById(1L)).thenReturn(place);
        when(authService.getCurrentUserId()).thenReturn(null);
        when(authService.isCurrentUserAdmin()).thenReturn(false);
        when(ratingService.findByPlace(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(rating)));
        when(ratingMapper.toDto(rating)).thenReturn(dto);

        mvc.perform(get("/api/places/1/ratings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(3L))
                .andExpect(jsonPath("$.content[0].score").value(4));
    }
}
