package com.donorconnect.repositroy;

import com.donorconnect.entity.auth.User;
import com.donorconnect.enums.Enums.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email); // used during login only
    boolean existsByEmail(String email);// used during registration
    List<User> findByRole(UserRole role);
    List<User> findByStatus(UserStatus status);
}



