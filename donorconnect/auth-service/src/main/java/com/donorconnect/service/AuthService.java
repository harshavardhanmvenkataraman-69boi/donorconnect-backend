package com.donorconnect.service;

import com.donorconnect.dto.request.auth.*;
import com.donorconnect.entity.auth.AuditLog;
import com.donorconnect.entity.auth.User;
import com.donorconnect.enums.Enums.*;
import com.donorconnect.exception.*; // Imports all your custom exceptions
import com.donorconnect.repository.AuditLogRepository;
import com.donorconnect.repository.UserRepository;
import com.donorconnect.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    private final Map<String, String> resetTokens = new HashMap<>();

    public String setupFirstAdmin(Setupadminrequest req) {
        boolean adminExists = userRepository.findByRole(UserRole.ROLE_ADMIN).size() > 0;
        if (adminExists) {
            // TRIGGER: UserAlreadyExistsException
            throw new UserAlreadyExistsException("Admin system is already initialized. Cannot create another primary admin.");
        }
        User admin = User.builder()
                .name(req.getName())
                .email(req.getEmail())
                .phone(req.getPhone())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(UserRole.ROLE_ADMIN)
                .status(UserStatus.ACTIVE)
                .build();

        userRepository.save(admin);
        return "Admin created successfully";
    }

    public Map<String, String> login(LoginRequest req) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));

            String token = tokenProvider.generateToken(auth);

            // TRIGGER: ResourceNotFoundException (if DB is out of sync with AuthManager)
            User user = userRepository.findByEmail(req.getEmail())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "email", req.getEmail()));

            auditLogRepository.save(AuditLog.builder().userId(user.getUserId())
                    .action("LOGIN").resource("AUTH").timestamp(LocalDateTime.now()).build());

            Map<String, String> result = new HashMap<>();
            result.put("token", token);
            result.put("role", user.getRole().name());
            result.put("email", user.getEmail());
            result.put("name", user.getName());
            return result;

        } catch (AuthenticationException ex) {
            // This catches BadCredentialsException and triggers the 401 in your GlobalHandler
            throw new JwtAuthenticationException("Authentication failed: Invalid email or password");
        }
    }

    public User register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            // TRIGGER: UserAlreadyExistsException
            throw new UserAlreadyExistsException("An account with email " + req.getEmail() + " already exists.");
        }

        User user = User.builder()
                .name(req.getName())
                .email(req.getEmail())
                .phone(req.getPhone())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(req.getRole())
                .status(UserStatus.ACTIVE)
                .build();
        return userRepository.save(user);
    }

    public void changePassword(String email, ChangePasswordRequest req) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        if (!passwordEncoder.matches(req.getOldPassword(), user.getPassword())) {
            // TRIGGER: InvalidPasswordException
            throw new InvalidPasswordException("The current password you entered is incorrect.");
        }

        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
    }

    public String forgotPassword(String email) {
        userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        String token = UUID.randomUUID().toString();
        resetTokens.put(token, email);
        return "Reset token generated. Token: " + token;
    }

    public String resetPassword(ResetPasswordRequest req) {
        // Here you should ideally validate the token from the map first
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", req.getEmail()));

        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
        return "Password reset successful";
    }

    public Page<User> getAllUsers(Pageable pageable) {

        return userRepository.findAll(pageable);
    }
    public List<User> getUsersByRole(UserRole role) { return userRepository.findByRole(role); }

// Finding all "INACTIVE" users to see who hasn't logged in recently.

    public List<User> getUsersByStatus(UserStatus status) {
        return userRepository.findByStatus(status);
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId.toString()));
    }

    public void lockUser(Long userId) {
        User u = getUserById(userId);
        u.setStatus(UserStatus.LOCKED);
        userRepository.save(u);
    }

    public void unlockUser(Long userId) { // now they will call the helpdesk that they forgot the password and locked their account, after verifying their identity then only the account will be unlocked
        User u = getUserById(userId); u.setStatus(UserStatus.ACTIVE); userRepository.save(u);
    }
    public void deactivateUser(Long userId) { // delete the accounts tha haven't been used since a long
        User u = getUserById(userId); u.setStatus(UserStatus.INACTIVE); userRepository.save(u);
    }


    // It pulls every single log from the database but in a "smart" way.
    // Instead of a List, it returns a Page
    public Page<AuditLog> getAllAuditLogs(Pageable pageable) {
        return auditLogRepository.findAll(pageable); }

    // It filters the history to show only what one specific person did
    public List<AuditLog> getAuditLogsByUser(Long userId) {
        return auditLogRepository.findByUserId(userId); }

    // It filters by the type of event, such as "LOGIN", "DELETE_USER", or "UPDATE_BLOOD_STOCK
    public List<AuditLog> getAuditLogsByAction(String action) {
        return auditLogRepository.findByAction(action); }
}
