package com.fitness.backend.meal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fitness.backend.auth.ApiExceptionHandler;
import com.fitness.backend.meal.dto.DailySummaryResponse;
import com.fitness.backend.meal.dto.GoalSummary;
import com.fitness.backend.meal.dto.MealCreateRequest;
import com.fitness.backend.meal.dto.MealItemRequest;
import com.fitness.backend.meal.dto.MealItemResponse;
import com.fitness.backend.meal.dto.MealResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
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
class MealControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private MealService mealService;

    @InjectMocks
    private MealController mealController;

    @BeforeEach
    void setUp() {
        objectMapper = JsonMapper.builder().findAndAddModules().build();
        mockMvc = MockMvcBuilders.standaloneSetup(mealController)
            .setControllerAdvice(new ApiExceptionHandler())
            .build();
    }

    @Test
    void createMealShouldReturn201WhenPayloadIsValid() throws Exception {
                String payload = """
                        {
                            \"mealDate\": \"2026-08-18\",
                            \"mealType\": \"LUNCH\",
                            \"consumedAt\": null,
                            \"notes\": null
                        }
                        """;
        when(mealService.createMeal(eq("user@test.com"), any(MealCreateRequest.class))).thenReturn(sampleMealResponse());

        mockMvc.perform(post("/api/meals")
                .principal(authenticatedUser())
                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.mealType").value("LUNCH"));
    }

    @Test
    void listMealsShouldReturn200() throws Exception {
        when(mealService.listMealsByDate("user@test.com", LocalDate.of(2026, 8, 18))).thenReturn(List.of(sampleMealResponse()));

        mockMvc.perform(get("/api/meals?date=2026-08-18").principal(authenticatedUser()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].mealType").value("LUNCH"));
    }

    @Test
    void addMealItemShouldReturn200() throws Exception {
        MealItemRequest request = new MealItemRequest(100L, new BigDecimal("150"));
        when(mealService.addMealItem(eq("user@test.com"), eq(10L), any(MealItemRequest.class))).thenReturn(sampleMealResponse());

        mockMvc.perform(post("/api/meals/10/items")
                .principal(authenticatedUser())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.kcalTotal").value(200.0));
    }

    @Test
    void addMealItemShouldReturn400WhenPayloadInvalid() throws Exception {
        String invalidPayload = """
            {
              \"foodId\": 100,
              \"grams\": 0
            }
            """;

        mockMvc.perform(post("/api/meals/10/items")
                .principal(authenticatedUser())
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidPayload))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Invalid request data"));
    }

    @Test
    void deleteMealItemShouldReturn204() throws Exception {
        doNothing().when(mealService).deleteMealItem("user@test.com", 10L, 1L);

        mockMvc.perform(delete("/api/meals/10/items/1").principal(authenticatedUser()))
            .andExpect(status().isNoContent());
    }

    @Test
    void summaryShouldReturn200() throws Exception {
        when(mealService.getDailySummary("user@test.com", LocalDate.of(2026, 8, 18))).thenReturn(
            new DailySummaryResponse(
                LocalDate.of(2026, 8, 18),
                new BigDecimal("650.00"),
                new BigDecimal("80.00"),
                new BigDecimal("25.00"),
                new BigDecimal("15.00"),
                new GoalSummary(2000, new BigDecimal("250.00"), new BigDecimal("125.00"), new BigDecimal("55.56"))
            )
        );

        mockMvc.perform(get("/api/meals/summary?date=2026-08-18").principal(authenticatedUser()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.kcalTotal").value(650.0));
    }

    private MealResponse sampleMealResponse() {
        return new MealResponse(
            10L,
            LocalDate.of(2026, 8, 18),
            MealType.LUNCH,
            null,
            null,
            new BigDecimal("200.00"),
            new BigDecimal("30.00"),
            new BigDecimal("8.00"),
            new BigDecimal("2.00"),
            List.of(new MealItemResponse(
                1L,
                100L,
                "Arroz",
                new BigDecimal("150.00"),
                new BigDecimal("200.00"),
                new BigDecimal("30.00"),
                new BigDecimal("8.00"),
                new BigDecimal("2.00")
            ))
        );
    }

    private Authentication authenticatedUser() {
        return new UsernamePasswordAuthenticationToken("user@test.com", "N/A");
    }
}
