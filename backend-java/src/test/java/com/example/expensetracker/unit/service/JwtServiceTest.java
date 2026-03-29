package com.example.expensetracker.unit.service;

import com.example.expensetracker.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;
    private final String testSecret = "testSecretKeyForJwtTokenGeneration1234567890123456789012345678901234567890";
    private final long expirationMs = 604800000;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(testSecret, expirationMs);
    }

    @Test
    void generateToken_ShouldCreateValidToken() {
        Long userId = 1L;
        String token = jwtService.generateToken(userId);
        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void parseUserId_ShouldReturnCorrectUserId_WhenTokenIsValid() {
        Long userId = 1L;
        String token = jwtService.generateToken(userId);
        Long parsedId = jwtService.parseUserId(token);
        assertThat(parsedId).isEqualTo(userId);
    }

    @Test
    void parseUserId_ShouldReturnNull_WhenTokenIsInvalid() {
        String invalidToken = "invalid.token.value";
        Long parsedId = jwtService.parseUserId(invalidToken);
        assertThat(parsedId).isNull();  // теперь должно работать
    }
}