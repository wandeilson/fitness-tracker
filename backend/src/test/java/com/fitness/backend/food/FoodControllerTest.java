package com.fitness.backend.food;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fitness.backend.auth.ApiExceptionHandler;
import com.fitness.backend.food.dto.FoodResponse;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class FoodControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private FoodService foodService;

    @InjectMocks
    private FoodController foodController;

    @BeforeEach
    void setUp() {
        objectMapper = JsonMapper.builder().findAndAddModules().build();
        mockMvc = MockMvcBuilders.standaloneSetup(foodController)
            .setControllerAdvice(new ApiExceptionHandler())
            .build();
    }

    @Test
    void listFoodsShouldReturn200() throws Exception {
        when(foodService.listFoods(null)).thenReturn(List.of(foodResponse(1L, "Arroz")));

        mockMvc.perform(get("/api/foods"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Arroz"));
    }

    @Test
    void getFoodShouldReturn200WhenFound() throws Exception {
        when(foodService.getFood(1L)).thenReturn(foodResponse(1L, "Arroz"));

        mockMvc.perform(get("/api/foods/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Arroz"));
    }

    @Test
    void getFoodShouldReturn400WhenNotFound() throws Exception {
        when(foodService.getFood(999L)).thenThrow(new IllegalArgumentException("Food not found"));

        mockMvc.perform(get("/api/foods/999"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Food not found"));
    }

    private FoodResponse foodResponse(Long id, String name) {
        return new FoodResponse(
            id,
            "TACO",
            "A001",
            name,
            new BigDecimal("128.00"),
            new BigDecimal("28.00"),
            new BigDecimal("2.50"),
            new BigDecimal("0.30")
        );
    }
}
