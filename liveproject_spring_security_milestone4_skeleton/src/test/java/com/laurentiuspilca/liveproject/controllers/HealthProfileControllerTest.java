package com.laurentiuspilca.liveproject.controllers;

import com.laurentiuspilca.liveproject.entities.HealthProfile;
import com.laurentiuspilca.liveproject.services.HealthProfileService;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(SpringExtension.class)
@WebMvcTest(HealthProfileController.class)
class HealthProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    HealthProfileService healthProfileService;

    @Test
    @DisplayName("Considering an authenticated request, assert that the returned HTTP status" +
            " is HTTP 200 OK and the service method is called.")
    public void addHealthProfileTest() throws Exception {
        HealthProfile healthProfile = new HealthProfile();
        healthProfile.setUsername("aUsername");

        mockMvc.perform(
                    post("/profile")
                            .with(jwt())
                            .content(asJsonString(healthProfile))
                            .contentType(MediaType.APPLICATION_JSON)
        )
                    .andExpect(status().isOk());

        verify(healthProfileService, times(1)).addHealthProfile(healthProfile);
    }

    @Test
    @DisplayName("Considering an unauthenticated request, assert that the returned HTTP status" +
            " is HTTP 403 Forbidden and the service method is not called.")
    public void addHealthProfileUnauthenticatedTest() throws Exception {
        HealthProfile healthProfile = new HealthProfile();
        healthProfile.setUsername("aUsername");

        mockMvc.perform(
                    post("/profile)")
                        .content(asJsonString(healthProfile))
                        .contentType(MediaType.APPLICATION_JSON)
        )
                    .andExpect(status().isForbidden());

        verify(healthProfileService, never()).addHealthProfile(any());
    }

    @Test
    @DisplayName("Considering an authenticated request, assert that the returned HTTP status" +
            " is HTTP 200 OK and the service method is called.")
    public void findHealthProfileTest() throws Exception {
        mockMvc.perform(
                    get("/profile/{username}","testuser")
                            .with(jwt())
        )
                    .andExpect(status().isOk());

        verify(healthProfileService, times(1)).findHealthProfile("testuser");
    }

    @Test
    @DisplayName("Considering an unauthenticated request, assert that the returned HTTP status" +
            " is HTTP 401 Unauthorized and the service method is not called.")
    public void findHealthProfileUnauthenticatedTest() throws Exception {
        mockMvc.perform(
                get("/profile/{username}","testuser")
        )
                .andExpect(status().isUnauthorized());

        verify(healthProfileService, never()).findHealthProfile("testuser");
    }

    @Test
    @DisplayName("Considering an authenticated request with a user having admin authority," +
            " assert that the returned HTTP status" +
            " is HTTP 200 OK and the service method is called.")
    public void deleteHealthProfileAuthenticatedWithAdminTest() throws Exception {
        mockMvc.perform(
                    delete("/profile/{username}", "testuser")
                            .with(jwt().authorities(() -> "ROLE_ADMIN"))
        )
                    .andExpect(status().isOk());

        verify(healthProfileService).deleteHealthProfile("testuser");
    }

    @Test
    @DisplayName("Considering an authenticated request with a non-admin user ," +
            " assert that the returned HTTP status" +
            " is HTTP 403 Forbidden and the service method is not called.")
    public void deleteHealthProfileAuthenticatedWithNonAdminUserTest() throws Exception {
        mockMvc.perform(
                delete("/profile/{username}", "testuser")
                        .with(jwt().authorities(() -> "ROLE_USER"))
        )
                .andExpect(status().isForbidden());

        verify(healthProfileService, never()).deleteHealthProfile("testuser");
    }

    @Test
    @DisplayName("Considering an unauthenticated request," +
            " assert that the returned HTTP status" +
            " is HTTP 403 Forbidden and the service method is not called.")
    public void deleteHealthProfileUnauthenticatedTest() throws Exception {
        mockMvc.perform(
                delete("/profile/{username}", "testuser")
        )
                .andExpect(status().isForbidden());

        verify(healthProfileService, never()).deleteHealthProfile("testuser");
    }

    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}