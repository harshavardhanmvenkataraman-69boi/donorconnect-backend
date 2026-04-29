package com.donorconnect.configservice.service;

import com.donorconnect.configservice.dto.request.SystemConfigRequest;
import com.donorconnect.configservice.entity.SystemConfig;
import com.donorconnect.configservice.enums.ConfigScope;
import com.donorconnect.configservice.exception.ResourceNotFoundException;
import com.donorconnect.configservice.repository.SystemConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SystemConfigService {

    private final SystemConfigRepository repo;

    public SystemConfig create(SystemConfigRequest req, String updatedBy) {
        if (repo.findByConfigKey(req.getKey()).isPresent()) {
            throw new IllegalArgumentException("Config key already exists: " + req.getKey());
        }
        SystemConfig cfg = SystemConfig.builder()
                .configKey(req.getKey())
                .configValue(req.getValue())
                .scope(req.getScope() != null
                        ? ConfigScope.valueOf(req.getScope().toUpperCase())
                        : ConfigScope.GLOBAL)
                .updatedBy(updatedBy)
                .build();
        log.info("Creating config key={} scope={} by={}", cfg.getConfigKey(), cfg.getScope(), updatedBy);
        return repo.save(cfg);
    }

    public List<SystemConfig> getAll() {
        return repo.findAll();
    }

    public SystemConfig getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SystemConfig", id));
    }

    public SystemConfig getByKey(String key) {
        return repo.findByConfigKey(key)
                .orElseThrow(() -> new ResourceNotFoundException("SystemConfig", "key", key));
    }

    public List<SystemConfig> getByScope(String scope) {
        return repo.findByScope(ConfigScope.valueOf(scope.toUpperCase()));
    }

    public SystemConfig update(Long id, SystemConfigRequest req, String updatedBy) {
        SystemConfig cfg = getById(id);
        if (req.getValue() != null) cfg.setConfigValue(req.getValue());
        if (req.getScope() != null) cfg.setScope(ConfigScope.valueOf(req.getScope().toUpperCase()));
        cfg.setUpdatedBy(updatedBy);
        log.info("Updating config id={} by={}", id, updatedBy);
        return repo.save(cfg);
    }

    public void delete(Long id) {
        getById(id); // throws if not found
        repo.deleteById(id);
        log.info("Deleted config id={}", id);
    }
}
