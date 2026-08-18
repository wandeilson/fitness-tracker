package com.fitness.backend.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.fitness.backend.profile.dto.ProfileRequest;
import com.fitness.backend.profile.dto.ProfileResponse;
import com.fitness.backend.user.ActivityLevel;
import com.fitness.backend.user.Sex;
import com.fitness.backend.user.User;
import com.fitness.backend.user.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProfileService profileService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail("user@test.com");
        user.setFullName("User Test");
        user.setAge(30);
        user.setWeightKg(80.0);
        user.setHeightCm(180);
        user.setSex(Sex.MALE);
        user.setActivityLevel(ActivityLevel.MODERATELY_ACTIVE);
    }

    @Test
    void getProfileShouldReturnMappedProfileData() {
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));

        ProfileResponse response = profileService.getProfile("user@test.com");

        assertEquals("user@test.com", response.email());
        assertEquals("User Test", response.fullName());
        assertEquals(30, response.age());
        assertEquals(80.0, response.weightKg());
        assertEquals(180, response.heightCm());
        assertEquals(Sex.MALE, response.sex());
        assertEquals(ActivityLevel.MODERATELY_ACTIVE, response.activityLevel());
    }

    @Test
    void updateProfileShouldUpdateFieldsIncludingEnums() {
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        ProfileRequest request = new ProfileRequest(
            "Novo Nome",
            35,
            82.5,
            181,
            Sex.FEMALE,
            ActivityLevel.VERY_ACTIVE
        );

        ProfileResponse response = profileService.updateProfile("user@test.com", request);

        assertEquals("Novo Nome", user.getFullName());
        assertEquals(35, user.getAge());
        assertEquals(82.5, user.getWeightKg());
        assertEquals(181, user.getHeightCm());
        assertEquals(Sex.FEMALE, user.getSex());
        assertEquals(ActivityLevel.VERY_ACTIVE, user.getActivityLevel());
        assertEquals("Novo Nome", response.fullName());
    }

    @Test
    void updateProfileShouldKeepFullNameWhenRequestNameIsBlank() {
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        ProfileRequest request = new ProfileRequest(
            "   ",
            31,
            81.0,
            179,
            Sex.MALE,
            ActivityLevel.LIGHTLY_ACTIVE
        );

        profileService.updateProfile("user@test.com", request);

        assertEquals("User Test", user.getFullName());
        assertEquals(31, user.getAge());
        assertEquals(ActivityLevel.LIGHTLY_ACTIVE, user.getActivityLevel());
    }

    @Test
    void getProfileShouldThrowWhenUserDoesNotExist() {
        when(userRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> profileService.getProfile("missing@test.com")
        );

        assertEquals("User not found", ex.getMessage());
    }
}
