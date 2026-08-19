package com.fitness.backend.goal;

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
import com.fitness.backend.goal.dto.GoalRequest;
import com.fitness.backend.goal.dto.GoalResponse;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
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
class GoalControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private GoalService goalService;

    @InjectMocks
    private GoalController goalController;

    @BeforeEach
    void setUp() {
        objectMapper = JsonMapper.builder().findAndAddModules().build();
        mockMvc = MockMvcBuilders.standaloneSetup(goalController)
            .setControllerAdvice(new ApiExceptionHandler())
            .build();
    }

    @Test
    void getGoalShouldReturn200WhenAuthenticated() throws Exception {
        GoalResponse response = buildGoalResponse();
        when(goalService.getGoal("user@test.com")).thenReturn(response);

        mockMvc.perform(get("/api/goals").principal(authenticatedUser()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.calories").value(2000))
            .andExpect(jsonPath("$.carbsPercent").value(50.0))
            .andExpect(jsonPath("$.proteinPercent").value(25.0))
            .andExpect(jsonPath("$.fatPercent").value(25.0))
            .andExpect(jsonPath("$.validFrom").value(LocalDate.now().toString()));
    }

    @Test
    void upsertGoalShouldReturn200WhenPayloadIsValid() throws Exception {
        GoalRequest request = new GoalRequest(2000, 40.0, 30.0, 30.0);
        GoalResponse response = new GoalResponse(
            2000,
            new BigDecimal("40.00"), new BigDecimal("30.00"), new BigDecimal("30.00"),
            new BigDecimal("800.00"), new BigDecimal("600.00"), new BigDecimal("600.00"),
            new BigDecimal("200.00"), new BigDecimal("150.00"), new BigDecimal("66.67"),
            LocalDate.now(), null,
            Instant.parse("2026-08-18T12:00:00Z")
        );
        when(goalService.upsertGoal(eq("user@test.com"), any(GoalRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/goals")
                .principal(authenticatedUser())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.calories").value(2000))
            .andExpect(jsonPath("$.carbsPercent").value(40.0))
            .andExpect(jsonPath("$.proteinPercent").value(30.0))
            .andExpect(jsonPath("$.fatPercent").value(30.0))
            .andExpect(jsonPath("$.validFrom").value(LocalDate.now().toString()));
    }

    @Test
    void upsertGoalShouldReturn400WhenServiceRejectsPayload() throws Exception {
        GoalRequest request = new GoalRequest(2000, 40.0, 30.0, 20.0);
        when(goalService.upsertGoal(eq("user@test.com"), any(GoalRequest.class)))
            .thenThrow(new IllegalArgumentException("Macro percentages must sum exactly to 100"));

        mockMvc.perform(put("/api/goals")
                .principal(authenticatedUser())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Macro percentages must sum exactly to 100"));
    }

    @Test
    void upsertGoalShouldReturn400WhenPayloadFailsBeanValidation() throws Exception {
        String invalidPayload = """
            {
              "calories": 0,
              "carbsPercent": 50.0,
              "proteinPercent": 25.0,
              "fatPercent": 25.0
            }
            """;

        mockMvc.perform(put("/api/goals")
                .principal(authenticatedUser())
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidPayload))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Invalid request data"));
    }

    private GoalResponse buildGoalResponse() {
        return new GoalResponse(
            2000,
            new BigDecimal("50.00"), new BigDecimal("25.00"), new BigDecimal("25.00"),
            new BigDecimal("1000.00"), new BigDecimal("500.00"), new BigDecimal("500.00"),
            new BigDecimal("250.00"), new BigDecimal("125.00"), new BigDecimal("55.56"),
            LocalDate.now(), null,
            Instant.parse("2026-08-18T12:00:00Z")
        );
    }

    private Authentication authenticatedUser() {
        return new UsernamePasswordAuthenticationToken("user@test.com", "N/A");
    }
}
