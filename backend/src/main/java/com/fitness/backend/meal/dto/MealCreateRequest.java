package com.fitness.backend.meal.dto;

import com.fitness.backend.meal.MealType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.time.LocalDate;

public record MealCreateRequest(
    @NotNull LocalDate mealDate,
    @NotNull MealType mealType,
    Instant consumedAt,
    @Size(max = 500) String notes
) {
}
