package com.pit.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pit.domain.Place;
import com.pit.domain.PlaceStatus;
import com.pit.domain.User;
import com.pit.repository.PlaceRepository;
import com.pit.repository.RatingRepository;
import com.pit.repository.UserRepository;
import com.pit.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Locale;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RatingControllerTest {

    @Autowired MockMvc mvc;
    @Autowired PlaceRepository placeRepository;
    @Autowired UserRepository userRepository;
    @Autowired RatingRepository ratingRepository;
    @MockBean AuthService authService;
    @Autowired ObjectMapper objectMapper;

    User user;
    Place approvedPlace;

    @BeforeEach
    void setup() {
        Locale.setDefault(Locale.US);
        ratingRepository.deleteAll();
        placeRepository.deleteAll();
        userRepository.deleteAll();

        user = new User();
        user.setEmail("member@test.local");
        user.setPassword("pwd");
        userRepository.save(user);

        approvedPlace = new Place();
        approvedPlace.setName("Riad Andalucia");
        approvedPlace.setLat(31.62);
        approvedPlace.setLng(-7.99);
        approvedPlace.setStatus(PlaceStatus.APPROVED);
        approvedPlace.setCreatedBy(user);
        placeRepository.save(approvedPlace);

        when(authService.isCurrentUserAdmin()).thenReturn(false);
    }

    @Test
    @WithMockUser(username = "member@test.local", roles = {"USER"})
    void authenticatedUserCanPostRating() throws Exception {
        when(authService.getCurrentUserId()).thenReturn(user.getId());

        var payload = objectMapper.writeValueAsString(new RatingPayload(5, "Superbe"));

        mvc.perform(post("/api/places/{id}/ratings", approvedPlace.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(5))
                .andExpect(jsonPath("$.comment").value("Superbe"));
    }

    @Test
    @WithMockUser(username = "member@test.local", roles = {"USER"})
    void postingRatingOnPendingPlaceReturnsConflict() throws Exception {
        Place pending = new Place();
        pending.setName("Kasbah");
        pending.setLat(30.0);
        pending.setLng(-8.0);
        pending.setStatus(PlaceStatus.PENDING);
        pending.setCreatedBy(user);
        placeRepository.save(pending);

        when(authService.getCurrentUserId()).thenReturn(user.getId());

        var payload = objectMapper.writeValueAsString(new RatingPayload(4, "En attente"));

        mvc.perform(post("/api/places/{id}/ratings", pending.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isConflict());
    }

    @Test
    void anonymousCannotAccessPendingRatings() throws Exception {
        Place pending = new Place();
        pending.setName("Cascade Secrète");
        pending.setLat(30.2);
        pending.setLng(-9.1);
        pending.setStatus(PlaceStatus.PENDING);
        pending.setCreatedBy(user);
        placeRepository.save(pending);

        mvc.perform(get("/api/places/{id}/ratings", pending.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "member@test.local", roles = {"USER"})
    void controllerReturnsUnauthorizedWhenNoCurrentUserId() throws Exception {
        when(authService.getCurrentUserId()).thenReturn(null);

        var payload = objectMapper.writeValueAsString(new RatingPayload(3, "Note"));

        mvc.perform(post("/api/places/{id}/ratings", approvedPlace.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isUnauthorized());
    }

    record RatingPayload(int score, String comment) {}
}
