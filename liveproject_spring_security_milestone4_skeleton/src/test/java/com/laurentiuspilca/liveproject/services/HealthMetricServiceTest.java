package com.laurentiuspilca.liveproject.services;

import com.laurentiuspilca.liveproject.entities.HealthMetric;
import com.laurentiuspilca.liveproject.entities.HealthProfile;
import com.laurentiuspilca.liveproject.exceptions.NonExistentHealthProfileException;
import com.laurentiuspilca.liveproject.repositories.HealthMetricRepository;
import com.laurentiuspilca.liveproject.repositories.HealthProfileRepository;
import com.laurentiuspilca.liveproject.services.context.TestUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class HealthMetricServiceTest {

    @Autowired HealthMetricService metricService;

    @MockBean
    HealthMetricRepository metricRepository;

    @MockBean
    HealthProfileRepository profileRepository;

    @Test

    @TestUser(username = "testuser")
    @DisplayName("Considering a request is done to add a new metric record for the authenticated user," +
            " and the user profile exists " +
            " assert that the record is added to the database.")
    void addHealthMetricValidUserAuthenticatedTest() {
        HealthProfile healthProfile = new HealthProfile();
        healthProfile.setUsername("testuser");

        HealthMetric healthMetric = new HealthMetric();
        healthMetric.setProfile(healthProfile);

        when(profileRepository.findHealthProfileByUsername("testuser")).thenReturn(Optional.of(healthProfile));

        metricService.addHealthMetric(healthMetric);

        verify(metricRepository).save(healthMetric);
    }

    @Test
    @TestUser(username = "testuser")
    @DisplayName("Considering a request is done to add a new metric record for the authenticated user," +
            " and the user profile deesn't exist assert that the record is not added to the database " +
            " and the app throws an exception.")
    void addHealthMetricValidUserAuthenticatedProfileDoesntExistTest() {
        HealthProfile healthProfile = new HealthProfile();
        healthProfile.setUsername("testuser");

        HealthMetric healthMetric = new HealthMetric();
        healthMetric.setProfile(healthProfile);

        when(profileRepository.findHealthProfileByUsername("testuser")).thenReturn(Optional.empty());

        assertThrows(NonExistentHealthProfileException.class,
                () -> metricService.addHealthMetric(healthMetric));

        verify(metricRepository, never()).save(any());
    }

    @Test
    @TestUser(username = "otheruser")
    @DisplayName("Considering a request is done to add a new record for another user than " +
            " the authenticated user, assert that the record is not added to the database " +
            "and the app throws an exception.")
    void addHealthMetricDifferentUserAuthenticatedTest() {
        HealthProfile healthProfile = new HealthProfile();
        healthProfile.setUsername("testuser");

        HealthMetric healthMetric = new HealthMetric();
        healthMetric.setProfile(healthProfile);

        when(profileRepository.findHealthProfileByUsername("testuser")).thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class,
                () -> metricService.addHealthMetric(healthMetric));

        verify(metricRepository, never()).save(any());
    }

    @Test
    @TestUser(username = "testuser")
    @DisplayName("Considering a list of records is found in the database, " +
            "assert this list is returned by the method.")
    void findHealthMetricHistoryTest() {
        HealthProfile healthProfile = new HealthProfile();
        healthProfile.setUsername("testuser");
        HealthMetric healthMetric1 = new HealthMetric();
        healthMetric1.setProfile(healthProfile);
        HealthMetric healthMetric2 = new HealthMetric();
        healthMetric2.setProfile(healthProfile);

        when(metricRepository.findHealthMetricHistory("testuser")).thenReturn(List.of(healthMetric1, healthMetric2));

        List<HealthMetric> result = metricService.findHealthMetricHistory("testuser");

        assertEquals(2, result.size());
    }

    @Test
    @TestUser(username = "admin", authorities = "ROLE_ADMIN")
    @DisplayName("Considering a request is done by an admin user to remove health metric records" +
            " and the profile for the metrics exists" +
            " assert that the records are removed from the database.")
    void deleteHealthMetricForUserWithAdminTest() {
        HealthProfile healthProfile = new HealthProfile();
        healthProfile.setUsername("testuser");

        when(profileRepository.findHealthProfileByUsername("testuser")).thenReturn(Optional.of(healthProfile));

        metricService.deleteHealthMetricForUser("testuser");

        verify(metricRepository).deleteAllForUser(healthProfile);
    }

    @Test
    @TestUser(username = "admin", authorities = "ROLE_ADMIN")
    @DisplayName("Considering a request is done by an admin user to remove health metric records" +
            " and the profile for the metrics doesn't exist" +
            " assert that the records are not removed from the database" +
            " and the app throws an exception.")
    void deleteHealthMetricForUserWithAdminProfileDoesntExistTest() {
        HealthProfile healthProfile = new HealthProfile();
        healthProfile.setUsername("testuser");

        when(profileRepository.findHealthProfileByUsername("testuser")).thenReturn(Optional.empty());

        assertThrows(NonExistentHealthProfileException.class,
                () -> metricService.deleteHealthMetricForUser("testuser"));

        verify(metricRepository, never()).deleteAllForUser(healthProfile);
    }

    @Test
    @TestUser(username = "testuser", authorities = "ROLE_USER")
    @DisplayName("Considering a request is done by a non-admin user to remove a record" +
            " assert that the record is not removed from the database and" +
            " the application throws an exception.")
    void deleteHealthMetricForUserWithNonAdminTest() {
        assertThrows(AccessDeniedException.class,
                () -> metricService.deleteHealthMetricForUser("testuser"));

        verify(metricRepository, never()).deleteAllForUser(any());
    }


}