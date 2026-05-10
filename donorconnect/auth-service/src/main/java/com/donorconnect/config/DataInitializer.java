//package com.donorconnect.config;
//
//import com.donorconnect.entity.auth.User;
//import com.donorconnect.enums.Enums.UserRole;
//import com.donorconnect.enums.Enums.UserStatus;
//import com.donorconnect.repository.UserRepository;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Component;
//
//@Component
//@RequiredArgsConstructor
//@Slf4j
//public class DataInitializer implements CommandLineRunner {
//
//    private final UserRepository  userRepository;
//    private final PasswordEncoder passwordEncoder;
//
//    @Override
//    public void run(String... args) {
//        initializeDefaultAdmin();
//    }
//
//    private void initializeDefaultAdmin() {
//        if (userRepository.existsByEmail("admin@donorconnect.in")) {
//            log.info("Default admin already exists — skipping init");
//            return;
//        }
//
//        User admin = User.builder()
//                .name("System Administrator")
//                .email("admin@gmail.com")
//                .phone("0000000000")
//                .password(passwordEncoder.encode("Admin@123"))
//                .role(UserRole.ROLE_ADMIN)
//                .status(UserStatus.ACTIVE)
//                .build();
//
//        userRepository.save(admin);
//
//        log.info("========================================");
//        log.info("  Default admin created successfully");
//        log.info("  Email   : admin@gmail.com");
//        log.info("  Password: Admin@123");
//        log.info("  Role    : ROLE_ADMIN");
//        log.info("  CHANGE THIS PASSWORD AFTER FIRST LOGIN");
//        log.info("========================================");
//    }
//}