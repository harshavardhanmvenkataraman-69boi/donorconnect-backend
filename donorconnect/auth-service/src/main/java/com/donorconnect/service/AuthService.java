//package com.donorconnect.service;
//
//import com.donorconnect.dto.request.auth.*;
//import com.donorconnect.entity.auth.AuditLog;
//import com.donorconnect.entity.auth.User;
//import com.donorconnect.enums.Enums.*;
//import com.donorconnect.exception.*;
//import com.donorconnect.repository.AuditLogRepository;
//import com.donorconnect.repository.UserRepository;
//import com.donorconnect.security.JwtTokenProvider;
//
//import lombok.RequiredArgsConstructor;
//
//import org.springframework.data.domain.*;
//import org.springframework.security.authentication.*;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.AuthenticationException;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//import java.util.*;
//
//@Service
//@RequiredArgsConstructor
//public class AuthService {
//
//    private final UserRepository userRepository;
//    private final AuditLogRepository auditLogRepository;
//    private final PasswordEncoder passwordEncoder;
//    private final AuthenticationManager authenticationManager;
//    private final JwtTokenProvider tokenProvider;
//
//    private final Map<String, String> resetTokens = new HashMap<>();
//
////    public String setupFirstAdmin(Setupadminrequest req) {
////        boolean adminExists = userRepository.findByRole(UserRole.ROLE_ADMIN).size() > 0;
////        if (adminExists) {
////            throw new UserAlreadyExistsException("Admin system is already initialized. Cannot create another primary admin.");
////        }
////        User admin = User.builder()
////                .name(req.getName())
////                .email(req.getEmail())
////                .phone(req.getPhone())
////                .password(passwordEncoder.encode(req.getPassword()))
////                .role(UserRole.ROLE_ADMIN)
////                .status(UserStatus.ACTIVE)
////                .build();
////
////        userRepository.save(admin);
////        return "Admin created successfully";
////    }
//
//        public User register(RegisterRequest req) {
//        if (userRepository.existsByEmail(req.getEmail())) {
//            throw new UserAlreadyExistsException("An account with email " + req.getEmail() + " already exists.");
//        }
//
//        User user = User.builder()
//                .name(req.getName())
//                .email(req.getEmail())
//                .phone(req.getPhone())
//                .password(passwordEncoder.encode(req.getPassword()))
//                .role(req.getRole())
//                .status(UserStatus.ACTIVE)
//                .build();
//        User saved = userRepository.save(user);
//        return saved;
//    }
//
//
//    public Map<String, String> login(LoginRequest req) {
//        try {
//            // it calls customuserdetailservice to find user in database and uses its password encoder to check if hashed password mathches or not
//            // if not match then will go to catch block
//            Authentication auth = authenticationManager.authenticate(
//                    new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));
//
//            String token = tokenProvider.generateToken(auth);
//            // it will go to jwt token provider to create a token
//
//            // TRIGGER: ResourceNotFoundException (if DB is out of sync with AuthManager)
//            // some extra details to send back to frontend
//            User user = userRepository.findByEmail(req.getEmail())
//                    .orElseThrow(() -> new ResourceNotFoundException("User", "email", req.getEmail()));
//
//
//            // public info that you want to be shown
//            Map<String, String> result = new HashMap<>();
//            result.put("token", token);
//            result.put("role", user.getRole().name());
//            result.put("email", user.getEmail());
//            result.put("name", user.getName());
//            return result;
//
//        } catch (AuthenticationException ex) {
//            throw new JwtAuthenticationException("Authentication failed: Invalid email or password");
//        }
//    }
//
//
//    public void changePassword(String email, ChangePasswordRequest req) {
//        User user = userRepository.findByEmail(email)
//                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
//
//        // passwordEncoder.matches takes the raw old password from the DTO and
//        // compares it against the hashed password from the Entity
//        if (!passwordEncoder.matches(req.getOldPassword(), user.getPassword())) {
//            throw new InvalidPasswordException("The current password you entered is incorrect.");
//        }
//
//        // takes the new password from dto and passwordEncoder.encode hash that password
//        // and updates that hashed password in the entity
//        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
//        // saves it to the database
//        userRepository.save(user);
//    }
//
//    public String forgotPassword(String email) {
//        userRepository.findByEmail(email)
//                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
//
//        // it creates a universal unique identifier (UUID) which is a random string
//        // that is very unlikely to be duplicated
//        String token = UUID.randomUUID().toString();
//        resetTokens.put(token, email); // This is like a "Claim Check." Later, when the user provides the token, the system looks at this Map to see which email it belongs to.
//        return "Reset token generated. Token: " + token;
//    }
//
//    public String resetPassword(ResetPasswordRequest req) {
//        // Here you should ideally validate the token from the map first
//        User user = userRepository.findByEmail(req.getEmail())
//                .orElseThrow(() -> new ResourceNotFoundException("User", "email", req.getEmail()));
//
//        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
//        userRepository.save(user);
//        return "Password reset successful";
//    }
//
//    public void logout(String email) {
//        // 1. Verify the user exists
//        User user = userRepository.findByEmail(email)
//                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
//    }
//
//    public Page<User> getAllUsers(Pageable pageable) {
//
//        return userRepository.findAll(pageable);
//    }
//
//    public User getUserById(Long userId) {
//        return userRepository.findById(userId)
//                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId.toString()));
//    }
//
//    public List<User> getUsersByRole(UserRole role) { return userRepository.findByRole(role); }
//
//    public List<User> getUsersByStatus(UserStatus status) {
//        return userRepository.findByStatus(status);
//    }
//
//
//    public void lockUser(Long userId) {
//        User u = getUserById(userId);
//        u.setStatus(UserStatus.LOCKED);
//        userRepository.save(u);
//    }
//
//    public void unlockUser(Long userId) {
//        User u = getUserById(userId);
//        u.setStatus(UserStatus.ACTIVE);
//        userRepository.save(u);
//    }
//    public void deactivateUser(Long userId) {
//        User u = getUserById(userId);
//        u.setStatus(UserStatus.INACTIVE);
//        userRepository.save(u);
//    }
//
//    public Page<AuditLog> getAllAuditLogs(Pageable pageable) {
//
//        return auditLogRepository.findAll(pageable);
//    }
//
//    // It filters the history to show only what one specific person did
//    public List<AuditLog> getAuditLogsByUser(Long userId) {
//        return auditLogRepository.findByUserId(userId);
//    }
//
//    public List<AuditLog> getAuditLogsByAction(String action) {
//
//        return auditLogRepository.findByAction(action);
//    }
//
//}

package com.donorconnect.service;



import com.donorconnect.dto.request.auth.*;
import com.donorconnect.entity.auth.AuditLog;
import com.donorconnect.entity.auth.User;
import com.donorconnect.enums.Enums.*;
import com.donorconnect.exception.*;
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
    private final EmailService emailService;

    private final Map<String, String> resetTokens = new HashMap<>();


    public Map<String, String> login(LoginRequest req) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));

            String token = tokenProvider.generateToken(auth);

            User user = userRepository.findByEmail(req.getEmail())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "email", req.getEmail()));

            // ── Write audit log ──────────────────────────────────────────
            auditLogRepository.save(AuditLog.builder()
                    .userId(user.getUserId())
                    .userName(user.getName())
                    .userRole(user.getRole().name())
                    .action("LOGIN")
                    .resource("User")
                    .timestamp(java.time.LocalDateTime.now())
                    .build());

            Map<String, String> result = new HashMap<>();
            result.put("token", token);
            result.put("role", user.getRole().name());
            result.put("email", user.getEmail());
            result.put("name", user.getName());
            return result;

        } catch (AuthenticationException ex) {
            // ── Failed login audit ───────────────────────────────────────
            auditLogRepository.save(AuditLog.builder()
                    .userName(req.getEmail())
                    .userRole("UNKNOWN")
                    .action("FAILED_LOGIN")
                    .resource("User")
                    .timestamp(java.time.LocalDateTime.now())
                    .build());
            throw new JwtAuthenticationException("Authentication failed: Invalid email or password");
        }
    }

    public User register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new UserAlreadyExistsException("An account with email " + req.getEmail() + " already exists.");
        }
        User user = User.builder()
                .name(req.getName()).
                email(req.getEmail()).
                phone(req.getPhone())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(req.getRole())
                .status(UserStatus.ACTIVE)
                .build();
        User saved = userRepository.save(user);
        auditLogRepository.save(AuditLog.builder()
                .userId(saved.getUserId())
                .userName(saved.getName())
                .userRole(saved.getRole().name())
                .action("USER_CREATED")
                .resource("User")
                .timestamp(java.time.LocalDateTime.now()).build());
        return saved;
    }

    public void changePassword(String email, ChangePasswordRequest req) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        if (!passwordEncoder.matches(req.getOldPassword(), user.getPassword()))
            throw new InvalidPasswordException("The current password you entered is incorrect.");
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
        auditLogRepository.save(AuditLog.builder()
                .userId(user.getUserId()).
                userName(user.getName())
                .userRole(user.getRole().name())
                .action("PASSWORD_CHANGE")
                .resource("User")
                .timestamp(java.time.LocalDateTime.now()).build());
    }

    public String forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        //Universally Unique Identifier(128 bit number system), a random/unique string
        // it acts as a one-time secure key that bridges the gap between the user's email inbox and your serve
        String token = UUID.randomUUID().toString();
        resetTokens.put(token, email);
        emailService.sendPasswordResetToken(email, user.getName(), token);
        return "If an account exists for that email, a reset token has been sent to it.";
    }

    public String resetPassword(ResetPasswordRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", req.getEmail()));
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
        auditLogRepository.save(AuditLog.builder()
                .userId(user.getUserId())
                .userName(user.getName())
                .userRole(user.getRole().name())
                .action("PASSWORD_RESET")
                .resource("User")
                .timestamp(java.time.LocalDateTime.now())
                .build());
        return "Password reset successful";
    }

    public void logout(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        auditLogRepository.save(AuditLog.builder()
                .userId(user.getUserId())
                .userName(user.getName())
                .userRole(user.getRole().name())
                .action("LOGOUT")
                .resource("User")
                .timestamp(java.time.LocalDateTime.now())
                .build());
    }

    public Page<User> getAllUsers(Pageable pageable) {

        return userRepository.findAll(pageable);
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId.toString()));
    }

    public List<User> getUsersByRole(UserRole role) { return userRepository.findByRole(role); }

    public List<User> getUsersByStatus(UserStatus status) {
        return userRepository.findByStatus(status);
    }


    public void lockUser(Long userId) {
        User u = getUserById(userId);
        u.setStatus(UserStatus.LOCKED);
        userRepository.save(u);
    }

    public void unlockUser(Long userId) {
        User u = getUserById(userId); u.setStatus(UserStatus.ACTIVE); userRepository.save(u);
    }
    public void deactivateUser(Long userId) {
        User u = getUserById(userId); u.setStatus(UserStatus.INACTIVE); userRepository.save(u);
    }

    public Page<AuditLog> getAllAuditLogs(Pageable pageable) {
        return auditLogRepository.findAll(pageable);
    }

    // It filters the history to show only what one specific person did
    public List<AuditLog> getAuditLogsByUser(Long userId) {
        return auditLogRepository.findByUserId(userId);
    }

    public List<AuditLog> getAuditLogsByAction(String action) {
        return auditLogRepository.findByAction(action);
    }

}
