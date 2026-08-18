package com.fitness.backend.profile.dto;

import com.fitness.backend.user.ActivityLevel;
import com.fitness.backend.user.Sex;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ProfileRequest(
    @Size(max = 255) String fullName,
    @Positive Integer age,
    @Positive Double weightKg,
    @Positive Integer heightCm,
    Sex sex,
    ActivityLevel activityLevel
) {
}
