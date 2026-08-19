package com.fitness.backend.food;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.fitness.backend.food.dto.FoodResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FoodServiceTest {

    @Mock
    private FoodRepository foodRepository;

    @InjectMocks
    private FoodService foodService;

    @Test
    void listFoodsShouldReturnAllActiveFoodsWhenQueryIsNull() {
        when(foodRepository.findByActiveTrueOrderByNameAsc()).thenReturn(List.of(food("Arroz")));

        List<FoodResponse> response = foodService.listFoods(null);

        assertEquals(1, response.size());
    }

    @Test
    void listFoodsShouldReturnAllActiveFoodsWhenQueryIsBlank() {
        when(foodRepository.findByActiveTrueOrderByNameAsc()).thenReturn(List.of(food("Arroz"), food("Feijao")));

        List<FoodResponse> response = foodService.listFoods("  ");

        assertEquals(2, response.size());
        assertEquals("Arroz", response.getFirst().name());
    }

    @Test
    void listFoodsShouldFilterByQueryWhenProvided() {
        when(foodRepository.findByActiveTrueAndNameContainingIgnoreCaseOrderByNameAsc("ar")).thenReturn(List.of(food("Arroz")));

        List<FoodResponse> response = foodService.listFoods("ar");

        assertEquals(1, response.size());
        assertEquals("Arroz", response.getFirst().name());
    }

    @Test
    void getFoodShouldReturnFoodWhenItExists() {
        when(foodRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(food("Arroz")));

        FoodResponse response = foodService.getFood(1L);

        assertEquals("Arroz", response.name());
        assertEquals(new BigDecimal("28.00"), response.carbsPer100g());
    }

    @Test
    void getFoodShouldThrowWhenFoodDoesNotExist() {
        when(foodRepository.findByIdAndActiveTrue(99L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> foodService.getFood(99L));

        assertEquals("Food not found", ex.getMessage());
    }

    private Food food(String name) {
        Food food = new Food();
        food.setSource("TACO");
        food.setSourceCode("A001");
        food.setName(name);
        food.setKcalPer100g(new BigDecimal("128.00"));
        food.setCarbsPer100g(new BigDecimal("28.00"));
        food.setProteinPer100g(new BigDecimal("2.50"));
        food.setFatPer100g(new BigDecimal("0.30"));
        food.setActive(true);
        return food;
    }
}
