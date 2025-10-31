package com.pit.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import com.pit.domain.Place;
import com.pit.service.PlaceService;

import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminPlaceControllerTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    PlaceService placeService;

    @Test
    void anonymousIsRedirectedToLogin() throws Exception {
        mvc.perform(get("/admin/places"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(roles = "USER")
    void nonAdminGetsForbidden() throws Exception {
        mvc.perform(get("/admin/places"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanAccessDashboard() throws Exception {
        when(placeService.findByStatus(any(), any()))
                .thenReturn(new PageImpl<>(Collections.<Place>emptyList()));

        mvc.perform(get("/admin/places"))
                .andExpect(status().isOk());
    }
}
