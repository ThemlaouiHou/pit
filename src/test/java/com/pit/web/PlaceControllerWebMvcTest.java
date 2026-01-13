package com.pit.web;

import com.pit.domain.Place;
import com.pit.domain.PlaceStatus;
import com.pit.security.JwtService;
import com.pit.service.AuthService;
import com.pit.service.PlaceService;
import com.pit.web.dto.PlaceDto;
import com.pit.web.mapper.PlaceMapper;
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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = com.pit.web.controller.PlaceController.class)
@AutoConfigureMockMvc(addFilters = false)
class PlaceControllerWebMvcTest {

    @Autowired MockMvc mvc;
    @MockBean PlaceService placeService;
    @MockBean PlaceMapper placeMapper;
    @MockBean AuthService authService;
    @MockBean JwtService jwtService;
    @MockBean UserDetailsService userDetailsService;

    @Test
    void listRejectsInvalidStatus() throws Exception {
        when(authService.isCurrentUserAdmin()).thenReturn(false);

        mvc.perform(get("/api/places").param("status", "UNKNOWN"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(placeService);
    }

    @Test
    void listAllRequiresAdmin() throws Exception {
        when(authService.isCurrentUserAdmin()).thenReturn(false);

        mvc.perform(get("/api/places").param("status", "ALL"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(placeService);
    }

    @Test
    void listApprovedReturnsPage() throws Exception {
        when(authService.isCurrentUserAdmin()).thenReturn(false);

        Place place = new Place();
        place.setId(10L);
        place.setName("Kasbah");
        place.setDescription("Forteresse");
        place.setLat(30.0);
        place.setLng(-8.0);
        place.setStatus(PlaceStatus.APPROVED);

        PlaceDto dto = new PlaceDto(
                10L,
                "Kasbah",
                "Forteresse",
                30.0,
                -8.0,
                "APPROVED",
                0.0,
                0,
                Instant.parse("2026-01-01T00:00:00Z")
        );

        when(placeService.findByStatus(eq(PlaceStatus.APPROVED), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(place)));
        when(placeMapper.toDto(place)).thenReturn(dto);

        mvc.perform(get("/api/places"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(10L))
                .andExpect(jsonPath("$.content[0].status").value("APPROVED"));
    }

    @Test
    void getReturnsNotFoundWhenMissing() throws Exception {
        when(placeService.findById(99L))
                .thenThrow(new IllegalArgumentException("Place not found: 99"));

        mvc.perform(get("/api/places/99"))
                .andExpect(status().isNotFound());
    }
}
