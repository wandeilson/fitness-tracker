package com.fitness.backend.meal.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record MealItemRequest(
    @NotNull Long foodId,
    @NotNull @Positive BigDecimal grams
) {
}
