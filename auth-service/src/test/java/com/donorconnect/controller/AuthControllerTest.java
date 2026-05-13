package com.donorconnect.controller;

import com.donorconnect.dto.request.auth.LoginRequest;
import com.donorconnect.service.AuthService;
import com.donorconnect.exception.JwtAuthenticationException;
import com.donorconnect.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc

public class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void login_ShouldReturn401_WhenCredentialsAreInvalid() throws Exception {
        // Arrange: Force the service to throw our custom exception
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("wrong@user.com");
        loginRequest.setPassword("wrongpass");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new JwtAuthenticationException("Authentication failed: Invalid email or password"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized()) // Check for 401
                .andExpect(jsonPath("$.message").value("Authentication failed: Invalid email or password"));
    }

    @Test
    void getUser_ShouldReturn404_WhenUserNotFound() throws Exception {
        // Arrange
        when(authService.getUserById(99L))
                .thenThrow(new ResourceNotFoundException("User", "id", "99"));

        // Act & Assert: Assuming your endpoint is /api/auth/users/{id}
        mockMvc.perform(post("/api/auth/users/99"))
                .andExpect(status().isNotFound()) // Check for 404
                .andExpect(jsonPath("$.status").value("error"));
    }
}