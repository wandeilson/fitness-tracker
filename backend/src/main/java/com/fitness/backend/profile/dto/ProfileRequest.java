package com.fitness.backend.profile.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ProfileRequest(
    @Size(max = 255) String fullName,
    @Positive Integer age,
    @Positive Double weightKg,
    @Positive Integer heightCm,
    @Size(max = 20) String sex,
    @Size(max = 50) String activityLevel
) {
}
