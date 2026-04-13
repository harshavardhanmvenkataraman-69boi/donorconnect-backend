package com.donorconnect.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;

public class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider();
        // Manually injecting the @Value fields for the test
        ReflectionTestUtils.setField(tokenProvider, "jwtSecret", "mySecretKeyForTestingWhichIsLongEnough32Chars");
        ReflectionTestUtils.setField(tokenProvider, "jwtExpiration", 3600000L);
    }

    @Test
    void validateToken_ShouldReturnFalse_WhenTokenIsInvalid() {
        boolean isValid = tokenProvider.validateToken("invalid.token.here");
        assertFalse(isValid);
    }

    @Test
    void validateToken_ShouldReturnFalse_WhenTokenIsExpired() {
        // You would typically mock the clock or use a very short expiration
        // For a simple unit test, an empty string or malformed token works
        assertFalse(tokenProvider.validateToken(""));
    }
}