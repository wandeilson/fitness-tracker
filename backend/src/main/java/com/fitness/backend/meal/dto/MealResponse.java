package com.fitness.backend.meal.dto;

import com.fitness.backend.meal.MealType;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record MealResponse(
    Long id,
    LocalDate mealDate,
    MealType mealType,
    Instant consumedAt,
    String notes,
    BigDecimal kcalTotal,
    BigDecimal carbsTotal,
    BigDecimal proteinTotal,
    BigDecimal fatTotal,
    List<MealItemResponse> items
) {
}
