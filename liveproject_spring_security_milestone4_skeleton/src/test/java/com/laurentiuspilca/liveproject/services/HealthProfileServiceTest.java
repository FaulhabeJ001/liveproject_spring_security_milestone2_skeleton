package com.laurentiuspilca.liveproject.services;

import com.laurentiuspilca.liveproject.entities.HealthProfile;
import com.laurentiuspilca.liveproject.exceptions.HealthProfileAlreadyExistsException;
import com.laurentiuspilca.liveproject.exceptions.NonExistentHealthProfileException;
import com.laurentiuspilca.liveproject.repositories.HealthProfileRepository;
import com.laurentiuspilca.liveproject.services.context.TestUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class HealthProfileServiceTest {

    @Autowired
    HealthProfileService healthProfileService;

    @MockBean
    HealthProfileRepository healthProfileRepository;

    @Test
    @TestUser(username = "testuser1")
    @DisplayName("Considering a request is done to add a new record for another user than" +
            " the authenticated user assert that the record is not added to the database" +
            " and the application throws an exception.")
    public void addHealthProfileWrongUserTests() {
        HealthProfile healthProfile = new HealthProfile();
        healthProfile.setUsername("testuser2");

        assertThrows(AccessDeniedException.class,
                () -> healthProfileService.addHealthProfile(healthProfile));
    }

    @Test
    @TestUser(username = "testuser")
    @DisplayName("Considering a request is done to add a new record for the authenticated user but" +
            " another profile record already exists for the same user," +
            " assert that the record is not added to the database and the application" +
            " throws an exception.")
    public void addHealthProfileHealthProfileExistsTests() {
        HealthProfile healthProfile = new HealthProfile();
        healthProfile.setUsername("testuser");

        when(healthProfileRepository.findHealthProfileByUsername("testuser")).thenReturn(Optional.of(healthProfile));

        assertThrows(HealthProfileAlreadyExistsException.class,
                () -> healthProfileService.addHealthProfile(healthProfile));
    }

    @Test
    @TestUser(username = "testuser")
    @DisplayName("Considering a request is done to add a new record for the authenticated user and" +
            " no other profile exists for the same user," +
            " assert that the record is added to the database.")
    public void addHealthProfileHealthProfileDoesntExistTests() {
        HealthProfile healthProfile = new HealthProfile();
        healthProfile.setUsername("testuser");

        when(healthProfileRepository.findHealthProfileByUsername("testuser")).thenReturn(Optional.empty());

        healthProfileService.addHealthProfile(healthProfile);

        verify(healthProfileRepository).save(healthProfile);
    }

    @Test
    @TestUser(username = "testuser")
    @DisplayName("Considering the call is done for another user than the authenticated one, " +
            "assert that calling the method throws an exception.")
    public void findHealthProfileWrongUserTests() {
        assertThrows(AccessDeniedException.class,
                () -> healthProfileService.findHealthProfile("testuser2"));
    }

    @Test
    @TestUser(username = "testuser")
    @DisplayName("Considering the call is done for the authenticated user but " +
            " no record exists for that user in the database, " +
            " assert that calling the method throws an exception.")
    public void findHealthProfileProfileDoesntExistTests() {
        when(healthProfileRepository.findHealthProfileByUsername("testuser")).thenReturn(Optional.empty());

        assertThrows(NonExistentHealthProfileException.class,
                () -> healthProfileService.findHealthProfile("testuser"));
    }

    @Test
    @TestUser(username = "testuser")
    @DisplayName("Considering the call is done for the authenticated user and " +
            " a record exists for that user in the database, " +
            " assert that calling the method returns the valid record.")
    public void findHealthProfileProfileExistsTests() {
        HealthProfile healthProfile = new HealthProfile();
        healthProfile.setUsername("testuser");

        when(healthProfileRepository.findHealthProfileByUsername(healthProfile.getUsername()))
                .thenReturn(Optional.of(healthProfile));

        HealthProfile result = healthProfileService.findHealthProfile(healthProfile.getUsername());

        assertEquals(healthProfile.getUsername(), result.getUsername());

    }

    @Test
    @TestUser(username = "admin", authorities = "ROLE_ADMIN")
    @DisplayName("Considering the call is done by and admin for a user and " +
            " a record exists for that user in the database, " +
            " assert that calling the method returns the valid record.")
    public void findHealthProfileAdminTests() {
        HealthProfile healthProfile = new HealthProfile();
        healthProfile.setUsername("testuser");

        when(healthProfileRepository.findHealthProfileByUsername(healthProfile.getUsername()))
                .thenReturn(Optional.of(healthProfile));

        HealthProfile result = healthProfileService.findHealthProfile(healthProfile.getUsername());

        assertEquals(healthProfile.getUsername(), result.getUsername());
    }

    @Test
    @TestUser(username = "testuser", authorities = "ROLE_USER")
    @DisplayName("Considering a request is done by a non-admin user to remove a record" +
            " assert that the record is not removed from the database and" +
            " the application throws an exception.")
    public void deleteHealthProfileNonAdminTest() {
        assertThrows(AccessDeniedException.class,
                () -> healthProfileService.deleteHealthProfile("testuser"));
    }

    @Test
    @TestUser(username = "admin", authorities = "ROLE_ADMIN")
    @DisplayName("Considering a request is done by an admin user to remove a record but" +
            " the record doesn't exists in the database " +
            " assert that the application throws an exception.")
    public void deleteHealthProfileAdminProfileNotPresentTest() {
        when(healthProfileRepository.findHealthProfileByUsername("testuser")).thenReturn(Optional.empty());

        assertThrows(NonExistentHealthProfileException.class,
                () -> healthProfileService.deleteHealthProfile("testuser"));
    }


    @Test
    @TestUser(username = "admin", authorities = "ROLE_ADMIN")
    @DisplayName("Considering a request is done by an admin user to remove a record and" +
            " the record exists in the database " +
            " assert that the record is removed from the database.")
    public void deleteHealthProfileAdminProfileExistsTest() {
        HealthProfile healthProfile = new HealthProfile();
        healthProfile.setUsername("testuser");

        when(healthProfileRepository.findHealthProfileByUsername("testuser")).thenReturn(Optional.of(healthProfile));

        healthProfileService.deleteHealthProfile("testuser");

        verify(healthProfileRepository).delete(healthProfile);
    }
}