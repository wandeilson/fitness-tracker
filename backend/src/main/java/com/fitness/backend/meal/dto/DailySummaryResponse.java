package com.fitness.backend.meal.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailySummaryResponse(
    LocalDate date,
    BigDecimal kcalTotal,
    BigDecimal carbsTotal,
    BigDecimal proteinTotal,
    BigDecimal fatTotal,
    GoalSummary goal
) {
}
