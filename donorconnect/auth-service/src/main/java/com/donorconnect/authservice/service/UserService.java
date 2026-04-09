package com.donorconnect.authservice.service;

import com.donorconnect.authservice.dto.UserDto;
import com.donorconnect.authservice.entity.*;
import com.donorconnect.authservice.enums.UserStatus;
import com.donorconnect.authservice.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor @Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;

    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    public UserDto getUserById(Long id) {
        return userRepository.findById(id).map(this::toDto)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
    }

    @Transactional
    public UserDto createUser(User user) {
        if (userRepository.existsByEmail(user.getEmail()))
            throw new RuntimeException("Email already exists: " + user.getEmail());
        User saved = userRepository.save(user);
        logAction(saved.getUserId(), "CREATE", "USER");
        return toDto(saved);
    }

    @Transactional
    public UserDto lockUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        user.setStatus(UserStatus.LOCKED);
        logAction(userId, "LOCK", "USER");
        return toDto(userRepository.save(user));
    }

    @Transactional
    public UserDto unlockUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        user.setStatus(UserStatus.ACTIVE);
        logAction(userId, "UNLOCK", "USER");
        return toDto(userRepository.save(user));
    }

    public void logAction(Long userId, String action, String resource) {
        auditLogRepository.save(AuditLog.builder().userId(userId).action(action).resource(resource).build());
    }

    private UserDto toDto(User user) {
        return UserDto.builder().userId(user.getUserId()).name(user.getName())
                .role(user.getRole()).email(user.getEmail()).phone(user.getPhone()).status(user.getStatus()).build();
    }
}
