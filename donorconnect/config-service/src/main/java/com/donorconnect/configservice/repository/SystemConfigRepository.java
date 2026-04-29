package com.donorconnect.configservice.repository;

import com.donorconnect.configservice.entity.SystemConfig;
import com.donorconnect.configservice.enums.ConfigScope;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface SystemConfigRepository extends JpaRepository<SystemConfig, Long> {
    Optional<SystemConfig> findByConfigKey(String key);
    List<SystemConfig> findByScope(ConfigScope scope);
}