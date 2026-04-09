package com.donorconnect.configservice.service;
import com.donorconnect.configservice.entity.SystemConfig;
import com.donorconnect.configservice.repository.SystemConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.*;

@Service @RequiredArgsConstructor
public class SystemConfigService {
    private final SystemConfigRepository repo;
    public List<SystemConfig> getAll() { return repo.findAll(); }
    public Optional<SystemConfig> getByKey(String key) { return repo.findByConfigKey(key); }
    public SystemConfig save(SystemConfig config) { return repo.save(config); }
    public void delete(Long id) { repo.deleteById(id); }
}
