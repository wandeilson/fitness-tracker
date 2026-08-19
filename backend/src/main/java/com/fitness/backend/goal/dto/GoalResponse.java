package com.fitness.backend.goal.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.math.BigDecimal;

public record GoalResponse(
    Integer calories,
    BigDecimal carbsPercent,
    BigDecimal proteinPercent,
    BigDecimal fatPercent,
    BigDecimal carbsCalories,
    BigDecimal proteinCalories,
    BigDecimal fatCalories,
    BigDecimal carbsG,
    BigDecimal proteinG,
    BigDecimal fatG,
    LocalDate validFrom,
    LocalDate validUntil,
    Instant updatedAt
) {
}
