package com.pit.web;

import com.pit.domain.Place;
import com.pit.domain.PlaceStatus;
import com.pit.domain.Role;
import com.pit.domain.User;
import com.pit.repository.PlaceRepository;
import com.pit.repository.UserRepository;
import com.pit.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.*;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PlaceControllerTest {

    @Autowired MockMvc mvc;
    @Autowired PlaceRepository placeRepository;
    @Autowired UserRepository userRepository;
    @MockBean AuthService authService;
    @Autowired ObjectMapper objectMapper;

    User author;

    @BeforeEach
    void setup() {
        Locale.setDefault(Locale.US); // ensure decimal dot in JSON expectations
        placeRepository.deleteAll();
        userRepository.deleteAll();

        author = new User();
        author.setEmail("user@test.local");
        author.setPassword("pwd");
        author.setRole(Role.USER);
        userRepository.save(author);

        User admin = new User();
        admin.setEmail("admin@test.local");
        admin.setPassword("pwd");
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);

        reset(authService);
        when(authService.isCurrentUserAdmin()).thenReturn(false);
        when(authService.getCurrentUserId()).thenReturn(null);
    }

    @Test
    void publicListReturnsOnlyApprovedPlaces() throws Exception {
        Place approved = savePlace("Plage Bleue", PlaceStatus.APPROVED);
        savePlace("Mont Secret", PlaceStatus.PENDING);

        mvc.perform(get("/api/places").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(approved.getId()))
                .andExpect(jsonPath("$.content[0].status").value("APPROVED"));
    }

    @Test
    void nonAdminCannotRequestPendingList() throws Exception {
        when(authService.isCurrentUserAdmin()).thenReturn(false);

        mvc.perform(get("/api/places").param("status", "PENDING"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@test.local", roles = {"ADMIN"})
    void adminCanQueryPendingPlaces() throws Exception {
        Place pending = savePlace("Oasis", PlaceStatus.PENDING);
        when(authService.isCurrentUserAdmin()).thenReturn(true);

        mvc.perform(get("/api/places").param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(pending.getId()))
                .andExpect(jsonPath("$.content[0].status").value("PENDING"));
    }

    @Test
    void pendingDetailHiddenFromPublic() throws Exception {
        Place pending = savePlace("Cascade", PlaceStatus.PENDING);
        when(authService.getCurrentUserId()).thenReturn(null);
        when(authService.isCurrentUserAdmin()).thenReturn(false);

        mvc.perform(get("/api/places/{id}", pending.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "user@test.local", roles = {"USER"})
    void ownerCanSeePendingDetail() throws Exception {
        Place pending = savePlace("Cascade", PlaceStatus.PENDING);
        when(authService.getCurrentUserId()).thenReturn(author.getId());
        when(authService.isCurrentUserAdmin()).thenReturn(false);

        mvc.perform(get("/api/places/{id}", pending.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void paginationRespectsPageSize() throws Exception {
        savePlace("A", PlaceStatus.APPROVED);
        savePlace("B", PlaceStatus.APPROVED);

        mvc.perform(get("/api/places")
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    @WithMockUser(username = "user@test.local", roles = {"USER"})
    void userCanCreatePlaceWhenAuthenticated() throws Exception {
        when(authService.getCurrentUserId()).thenReturn(author.getId());
        when(authService.isCurrentUserAdmin()).thenReturn(false);

        var payload = objectMapper.writeValueAsString(
                new CreatePlacePayload("Tour Hassan", "Monument historique", 34.0223, -6.8356)
        );

        mvc.perform(post("/api/places")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Tour Hassan"))
                .andExpect(jsonPath("$.status").value("PENDING"));

        assertThat(placeRepository.findAll())
                .anyMatch(p -> "Tour Hassan".equals(p.getName()) && p.getCreatedBy().getId().equals(author.getId()));
    }

    @Test
    @WithMockUser(username = "user@test.local", roles = {"USER"})
    void createPlaceRejectsInvalidLatitude() throws Exception {
        when(authService.getCurrentUserId()).thenReturn(author.getId());
        when(authService.isCurrentUserAdmin()).thenReturn(false);

        var payload = objectMapper.writeValueAsString(
                new CreatePlacePayload("Invalid", "Bad lat", 120.0, -6.0)
        );

        mvc.perform(post("/api/places")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unauthenticatedUserCannotCreatePlace() throws Exception {
        var payload = objectMapper.writeValueAsString(
                new CreatePlacePayload("Zagora", "Désert", 29.711, -7.952)
        );

        mvc.perform(post("/api/places")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user@test.local", roles = {"USER"})
    void nonAdminCannotDeletePlaceViaApi() throws Exception {
        Place pending = savePlace("Interdit", PlaceStatus.PENDING);

        mvc.perform(delete("/api/places/{id}", pending.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@test.local", roles = {"ADMIN"})
    void adminCanDeletePlaceViaApi() throws Exception {
        Place pending = savePlace("Ruines", PlaceStatus.PENDING);
        when(authService.isCurrentUserAdmin()).thenReturn(true);

        mvc.perform(delete("/api/places/{id}", pending.getId()))
                .andExpect(status().isNoContent());

        assertThat(placeRepository.findById(pending.getId())).isEmpty();
    }

    record CreatePlacePayload(String name, String description, double lat, double lng) {}

    private Place savePlace(String name, PlaceStatus status) {
        Place place = new Place();
        place.setName(name);
        place.setDescription("Description " + name);
        place.setLat(33.0);
        place.setLng(-7.0);
        place.setStatus(status);
        place.setCreatedBy(author);
        return placeRepository.save(place);
    }
}
