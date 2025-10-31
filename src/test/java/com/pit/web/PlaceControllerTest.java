package com.pit.web;

import com.pit.domain.Place;
import com.pit.domain.PlaceStatus;
import com.pit.domain.Role;
import com.pit.domain.User;
import com.pit.repository.PlaceRepository;
import com.pit.repository.UserRepository;
import com.pit.service.RatingService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PlaceControllerTest {

    @Autowired MockMvc mvc;
    @Autowired PlaceRepository placeRepository;
    @Autowired UserRepository userRepository;
    @Autowired RatingService ratingService;

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
        mvc.perform(get("/api/places").param("status", "PENDING"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@test.local", roles = {"ADMIN"})
    void adminCanQueryPendingPlaces() throws Exception {
        Place pending = savePlace("Oasis", PlaceStatus.PENDING);

        mvc.perform(get("/api/places").param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(pending.getId()))
                .andExpect(jsonPath("$.content[0].status").value("PENDING"));
    }

    @Test
    void pendingDetailHiddenFromPublic() throws Exception {
        Place pending = savePlace("Cascade", PlaceStatus.PENDING);

        mvc.perform(get("/api/places/{id}", pending.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "user@test.local", roles = {"USER"})
    void ownerCanSeePendingDetail() throws Exception {
        Place pending = savePlace("Cascade", PlaceStatus.PENDING);

        mvc.perform(get("/api/places/{id}", pending.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void ratingsEndpointReturnsDataForApprovedPlace() throws Exception {
        Place approved = savePlace("Kasbah", PlaceStatus.APPROVED);
        ratingService.rate(approved.getId(), author.getId(), 4, "Très beau site");

        mvc.perform(get("/api/places/{id}/ratings", approved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].score").value(4));

        Place reloaded = placeRepository.findById(approved.getId()).orElseThrow();
        assertThat(reloaded.getAvgRating()).isEqualTo(4.0);
        assertThat(reloaded.getRatingsCount()).isEqualTo(1);
    }

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
