package com.fitness.backend.profile.dto;

public record ProfileResponse(
    String email,
    String fullName,
    Integer age,
    Double weightKg,
    Integer heightCm,
    String sex,
    String activityLevel
) {
}
