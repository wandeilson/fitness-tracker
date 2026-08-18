package com.fitness.backend.meal.dto;

import java.math.BigDecimal;

public record MealItemResponse(
    Long id,
    Long foodId,
    String foodName,
    BigDecimal grams,
    BigDecimal kcalConsumed,
    BigDecimal carbsConsumed,
    BigDecimal proteinConsumed,
    BigDecimal fatConsumed
) {
}
