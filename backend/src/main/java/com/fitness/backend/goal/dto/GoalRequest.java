package com.fitness.backend.goal.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record GoalRequest(
    @NotNull @Positive Integer calories,
    @PositiveOrZero Double carbsPercent,
    @PositiveOrZero Double proteinPercent,
    @PositiveOrZero Double fatPercent
) {
}
