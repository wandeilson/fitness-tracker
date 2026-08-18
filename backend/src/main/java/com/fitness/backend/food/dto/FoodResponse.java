package com.fitness.backend.food.dto;

import java.math.BigDecimal;

public record FoodResponse(
    Long id,
    String source,
    String sourceCode,
    String name,
    BigDecimal kcalPer100g,
    BigDecimal carbsPer100g,
    BigDecimal proteinPer100g,
    BigDecimal fatPer100g
) {
}
