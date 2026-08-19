package com.fitness.backend.meal.dto;

import java.math.BigDecimal;

public record GoalSummary(
    Integer calories,
    BigDecimal carbsG,
    BigDecimal proteinG,
    BigDecimal fatG
) {
}
