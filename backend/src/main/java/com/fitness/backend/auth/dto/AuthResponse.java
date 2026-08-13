package com.fitness.backend.auth.dto;

public record AuthResponse(
    String token,
    String email,
    String fullName
) {
}
