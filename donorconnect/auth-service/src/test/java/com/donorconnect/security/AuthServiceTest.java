package com.donorconnect.security;

import com.donorconnect.dto.request.auth.RegisterRequest;
import com.donorconnect.entity.auth.User;
import com.donorconnect.enums.Enums.UserRole;
import com.donorconnect.exception.UserAlreadyExistsException;
import com.donorconnect.repositroy.AuditLogRepository;
import com.donorconnect.repositroy.UserRepository;
import com.donorconnect.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private AuditLogRepository auditLogRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtTokenProvider tokenProvider;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@donorconnect.com");
        registerRequest.setPassword("password123");
        registerRequest.setName("Test User");
        registerRequest.setRole(UserRole.ROLE_DONOR);
    }

    @Test
    void register_ShouldSaveUser_WhenEmailIsUnique() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_pass");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        User savedUser = authService.register(registerRequest);

        // Assert
        assertNotNull(savedUser);
        assertEquals("test@donorconnect.com", savedUser.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_ShouldThrowException_WhenEmailExists() {
        // Arrange
        when(userRepository.existsByEmail("test@donorconnect.com")).thenReturn(true);

        // Act & Assert
        assertThrows(UserAlreadyExistsException.class, () -> {
            authService.register(registerRequest);
        });
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUserById_ShouldThrowResourceNotFound_WhenUserDoesNotExist() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> authService.getUserById(1L));
    }
}