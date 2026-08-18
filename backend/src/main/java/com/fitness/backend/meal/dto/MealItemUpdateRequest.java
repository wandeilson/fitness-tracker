package com.fitness.backend.meal.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record MealItemUpdateRequest(
    @NotNull @Positive BigDecimal grams
) {
}
