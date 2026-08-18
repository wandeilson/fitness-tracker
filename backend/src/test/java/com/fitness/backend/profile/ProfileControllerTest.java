package com.fitness.backend.profile;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fitness.backend.auth.ApiExceptionHandler;
import com.fitness.backend.profile.dto.ProfileRequest;
import com.fitness.backend.profile.dto.ProfileResponse;
import com.fitness.backend.user.ActivityLevel;
import com.fitness.backend.user.Sex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class ProfileControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private ProfileService profileService;

    @InjectMocks
    private ProfileController profileController;

    @BeforeEach
    void setUp() {
        objectMapper = JsonMapper.builder().findAndAddModules().build();
        mockMvc = MockMvcBuilders.standaloneSetup(profileController)
            .setControllerAdvice(new ApiExceptionHandler())
            .build();
    }

    @Test
    void getProfileShouldReturn200WhenAuthenticated() throws Exception {
        ProfileResponse response = new ProfileResponse(
            "user@test.com",
            "User Test",
            30,
            80.0,
            180,
            Sex.MALE,
            ActivityLevel.MODERATELY_ACTIVE
        );

        when(profileService.getProfile("user@test.com")).thenReturn(response);

        mockMvc.perform(get("/api/profile").principal(authenticatedUser()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("user@test.com"))
            .andExpect(jsonPath("$.fullName").value("User Test"))
            .andExpect(jsonPath("$.sex").value("MALE"))
            .andExpect(jsonPath("$.activityLevel").value("MODERATELY_ACTIVE"));
    }

    @Test
    void updateProfileShouldReturn200WhenPayloadIsValid() throws Exception {
        ProfileRequest request = new ProfileRequest(
            "Novo Nome",
            35,
            82.5,
            181,
            Sex.FEMALE,
            ActivityLevel.VERY_ACTIVE
        );
        ProfileResponse response = new ProfileResponse(
            "user@test.com",
            "Novo Nome",
            35,
            82.5,
            181,
            Sex.FEMALE,
            ActivityLevel.VERY_ACTIVE
        );

        when(profileService.updateProfile(eq("user@test.com"), any(ProfileRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/profile")
            .principal(authenticatedUser())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.fullName").value("Novo Nome"))
            .andExpect(jsonPath("$.sex").value("FEMALE"))
            .andExpect(jsonPath("$.activityLevel").value("VERY_ACTIVE"));
    }

    @Test
    void updateProfileShouldReturn400WhenPayloadIsInvalid() throws Exception {
        String invalidPayload = """
            {
              \"fullName\": \"Nome\",
              \"age\": -1,
              \"weightKg\": 80.0,
              \"heightCm\": 180,
              \"sex\": \"MALE\",
              \"activityLevel\": \"SEDENTARY\"
            }
            """;

        mockMvc.perform(put("/api/profile")
            .principal(authenticatedUser())
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidPayload))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Invalid request data"));
    }

    @Test
    void updateProfileShouldReturn400WhenEnumValueIsInvalid() throws Exception {
        String invalidEnumPayload = """
            {
              \"fullName\": \"Nome\",
              \"age\": 31,
              \"weightKg\": 80.0,
              \"heightCm\": 180,
              \"sex\": \"OTHER\",
              \"activityLevel\": \"SEDENTARY\"
            }
            """;

        mockMvc.perform(put("/api/profile")
            .principal(authenticatedUser())
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidEnumPayload))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Invalid enum value for profile fields"));
    }

    private Authentication authenticatedUser() {
        return new UsernamePasswordAuthenticationToken("user@test.com", "N/A");
    }
}
