package com.laurentiuspilca.liveproject.controllers;

import com.laurentiuspilca.liveproject.entities.HealthMetric;
import com.laurentiuspilca.liveproject.entities.enums.HealthMetricType;
import com.laurentiuspilca.liveproject.services.HealthMetricService;
import org.codehaus.jackson.map.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(HealthMetricController.class)
class HealthMetricControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    HealthMetricService healthMetricService;

    @Test
    @DisplayName("Considering an authenticated request, assert that the returned HTTP status" +
            " is HTTP 200 OK and the service method is called.")
    public void addHealthMetricTest() throws Exception {
        mockMvc.perform(
                    post("/metric")
                            .with(jwt())
                            .content(asJsonString(new HealthMetric()))
                            .contentType(MediaType.APPLICATION_JSON)
        )
                    .andExpect(status().isOk());

        verify(healthMetricService).addHealthMetric(any());
    }

    @Test
    @DisplayName("Considering an unauthenticated request, assert that the returned HTTP status" +
            " is HTTP 403 Forbidden and the service method is not called.")
    public void addHealthMetricUnauthenticatedTest() throws Exception {
        mockMvc.perform(
                post("/metric")
                        .content(asJsonString(new HealthMetric()))
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isForbidden());

        verify(healthMetricService, never()).addHealthMetric(any());
    }

    @Test
    @DisplayName("Considering an authenticated request, assert that the returned HTTP status" +
            " is HTTP 200 OK and the service method is called.")
    public void findHealthMetricsTest() throws Exception {
        HealthMetric healthMetric = new HealthMetric();
        healthMetric.setType(HealthMetricType.BLOOD_OXYGEN_LEVEL);
        healthMetric.setValue(1.0);

        when(healthMetricService.findHealthMetricHistory("testuser")).thenReturn(List.of(healthMetric));

        mockMvc.perform(
                    get("/metric/{username}", "testuser")
                        .with(jwt())
        )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].value", Matchers.is(1.0)));

        verify(healthMetricService).findHealthMetricHistory(any());
    }

    @Test
    @DisplayName("Considering an unauthenticated request, assert that the returned HTTP status" +
            " is HTTP 401 Unauthorized and the service method is not called.")
    public void findHealthMetricsUnauthenticatedTest() throws Exception {
        mockMvc.perform(
                get("/metric/{username}", "testuser")
        )
                .andExpect(status().isUnauthorized());

        verify(healthMetricService, never()).findHealthMetricHistory(any());
    }

    @Test
    @DisplayName("Considering an authenticated request with a user having admin authority," +
            " assert that the returned HTTP status" +
            " is HTTP 200 OK and the service method is called.")
    public void deleteHealthMetricForUserAdminTest() throws Exception {
        mockMvc.perform(
                    delete("/metric/{username}", "testuser")
                            .with(jwt().authorities(() -> "ROLE_ADMIN"))
        )
                            .andExpect(status().isOk());

        verify(healthMetricService).deleteHealthMetricForUser("testuser");
    }

    @Test
    @DisplayName("Considering an authenticated request with a non-admin user ," +
            " assert that the returned HTTP status" +
            " is HTTP 403 Forbidden and the service method is not called.")
    public void deleteHealthMetricForNonAdminUserTest() throws Exception {
        mockMvc.perform(
                    delete("/metric/{username}", "testuser")
                        .with(jwt().authorities(() -> "ROLE_USER"))
        )
                    .andExpect(status().isForbidden());

        verify(healthMetricService, never()).deleteHealthMetricForUser(any());
    }

    @Test
    @DisplayName("Considering an unauthenticated request," +
            " assert that the returned HTTP status" +
            " is HTTP 403 Forbidden and the service method is not called.")
    public void deleteHealthMetricUnauthenticatedTest() throws Exception {
        mockMvc.perform(
                delete("/metric/{username}", "testuser")
                        .with(jwt().authorities(() -> "ROLE_USER"))
        )
                .andExpect(status().isForbidden());

        verify(healthMetricService, never()).deleteHealthMetricForUser(any());
    }

    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}