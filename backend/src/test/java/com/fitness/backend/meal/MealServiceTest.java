package com.fitness.backend.meal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fitness.backend.food.Food;
import com.fitness.backend.food.FoodRepository;
import com.fitness.backend.meal.dto.DailySummaryResponse;
import com.fitness.backend.meal.dto.MealCreateRequest;
import com.fitness.backend.meal.dto.MealItemRequest;
import com.fitness.backend.meal.dto.MealItemUpdateRequest;
import com.fitness.backend.meal.dto.MealResponse;
import com.fitness.backend.user.User;
import com.fitness.backend.user.UserRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class MealServiceTest {

    @Mock
    private MealRepository mealRepository;

    @Mock
    private MealItemRepository mealItemRepository;

    @Mock
    private FoodRepository foodRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MealService mealService;

    private User user;
    private Meal meal;
    private Food food;

    @BeforeEach
    void setUp() {
        user = new User();
        ReflectionTestUtils.setField(user, "id", 1L);
        user.setEmail("user@test.com");

        meal = new Meal();
        ReflectionTestUtils.setField(meal, "id", 10L);
        meal.setUser(user);
        meal.setMealDate(LocalDate.of(2026, 8, 18));
        meal.setMealType(MealType.LUNCH);
        meal.setCreatedAt(Instant.parse("2026-08-18T12:00:00Z"));

        food = new Food();
        ReflectionTestUtils.setField(food, "id", 100L);
        food.setName("Arroz");
        food.setKcalPer100g(new BigDecimal("128.00"));
        food.setCarbsPer100g(new BigDecimal("28.00"));
        food.setProteinPer100g(new BigDecimal("2.50"));
        food.setFatPer100g(new BigDecimal("0.30"));
        food.setActive(true);

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
    }

    @Test
    void createMealShouldPersistAndReturnMeal() {
        MealCreateRequest request = new MealCreateRequest(
            LocalDate.of(2026, 8, 18),
            MealType.BREAKFAST,
            null,
            "Cafe"
        );

        when(mealRepository.save(any(Meal.class))).thenAnswer(invocation -> {
            Meal saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 11L);
            return saved;
        });

        MealResponse response = mealService.createMeal("user@test.com", request);

        assertEquals(MealType.BREAKFAST, response.mealType());
        assertEquals(new BigDecimal("0.00"), response.kcalTotal());
    }

    @Test
    void addMealItemShouldCalculateConsumedMacrosByGrams() {
        when(mealRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(meal));
        when(foodRepository.findByIdAndActiveTrue(100L)).thenReturn(Optional.of(food));
        when(mealItemRepository.save(any(MealItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MealItem item = new MealItem();
        ReflectionTestUtils.setField(item, "id", 1L);
        item.setMeal(meal);
        item.setFood(food);
        item.setGrams(new BigDecimal("150.00"));
        item.setKcalConsumed(new BigDecimal("192.00"));
        item.setCarbsConsumed(new BigDecimal("42.00"));
        item.setProteinConsumed(new BigDecimal("3.75"));
        item.setFatConsumed(new BigDecimal("0.45"));
        item.setCreatedAt(Instant.now());

        when(mealItemRepository.findByMealIdOrderByCreatedAtAsc(10L)).thenReturn(List.of(item));

        MealResponse response = mealService.addMealItem("user@test.com", 10L, new MealItemRequest(100L, new BigDecimal("150")));

        assertEquals(new BigDecimal("192.00"), response.kcalTotal());
        assertEquals(new BigDecimal("42.00"), response.carbsTotal());
        assertEquals(1, response.items().size());
    }

    @Test
    void addMealItemShouldThrowWhenMealDoesNotBelongToUser() {
        when(mealRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> mealService.addMealItem("user@test.com", 10L, new MealItemRequest(100L, new BigDecimal("100")))
        );

        assertEquals("Meal not found", ex.getMessage());
    }

    @Test
    void updateMealItemShouldThrowWhenItemDoesNotExist() {
        when(mealRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(meal));
        when(mealItemRepository.findByIdAndMealId(99L, 10L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> mealService.updateMealItem("user@test.com", 10L, 99L, new MealItemUpdateRequest(new BigDecimal("120")))
        );

        assertEquals("Meal item not found", ex.getMessage());
    }

    @Test
    void deleteMealItemShouldThrowWhenItemDoesNotExist() {
        when(mealRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(meal));
        when(mealItemRepository.findByIdAndMealId(99L, 10L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> mealService.deleteMealItem("user@test.com", 10L, 99L)
        );

        assertEquals("Meal item not found", ex.getMessage());
    }

    @Test
    void getDailySummaryShouldAggregateTotalsFromAllMeals() {
        when(mealRepository.findByUserIdAndMealDateOrderByCreatedAtAsc(1L, LocalDate.of(2026, 8, 18))).thenReturn(List.of(meal));

        MealItem item = new MealItem();
        item.setMeal(meal);
        item.setFood(food);
        item.setGrams(new BigDecimal("100.00"));
        item.setKcalConsumed(new BigDecimal("128.00"));
        item.setCarbsConsumed(new BigDecimal("28.00"));
        item.setProteinConsumed(new BigDecimal("2.50"));
        item.setFatConsumed(new BigDecimal("0.30"));
        item.setCreatedAt(Instant.now());

        when(mealItemRepository.findByMealIdOrderByCreatedAtAsc(10L)).thenReturn(List.of(item));

        DailySummaryResponse response = mealService.getDailySummary("user@test.com", LocalDate.of(2026, 8, 18));

        assertEquals(new BigDecimal("128.00"), response.kcalTotal());
        assertEquals(new BigDecimal("28.00"), response.carbsTotal());
        assertEquals(new BigDecimal("2.50"), response.proteinTotal());
        assertEquals(new BigDecimal("0.30"), response.fatTotal());
    }
}
