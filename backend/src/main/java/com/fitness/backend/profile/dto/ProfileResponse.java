package com.fitness.backend.profile.dto;

import com.fitness.backend.user.ActivityLevel;
import com.fitness.backend.user.Sex;

public record ProfileResponse(
    String email,
    String fullName,
    Integer age,
    Double weightKg,
    Integer heightCm,
    Sex sex,
    ActivityLevel activityLevel
) {
}
