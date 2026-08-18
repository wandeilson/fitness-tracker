package com.fitness.backend.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret", "01234567890123456789012345678901");
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", 3600000L);
    }

    @Test
    void shouldGenerateTokenAndExtractSubject() {
        UserDetails userDetails = User.withUsername("user@test.com").password("N/A").roles("USER").build();

        String token = jwtService.generateToken(userDetails);
        String username = jwtService.extractUsername(token);

        assertEquals("user@test.com", username);
        assertTrue(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void shouldThrowWhenTokenIsExpired() {
        UserDetails userDetails = User.withUsername("user@test.com").password("N/A").roles("USER").build();
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", -1000L);

        String token = jwtService.generateToken(userDetails);

        assertThrows(ExpiredJwtException.class, () -> jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void shouldThrowWhenTokenIsMalformed() {
        assertThrows(JwtException.class, () -> jwtService.extractUsername("invalid.token.value"));
    }

    @Test
    void shouldReturnFalseWhenTokenUserDoesNotMatchUserDetails() {
        UserDetails tokenOwner = User.withUsername("owner@test.com").password("N/A").roles("USER").build();
        UserDetails anotherUser = User.withUsername("other@test.com").password("N/A").roles("USER").build();

        String token = jwtService.generateToken(tokenOwner);

        assertFalse(jwtService.isTokenValid(token, anotherUser));
    }
}
