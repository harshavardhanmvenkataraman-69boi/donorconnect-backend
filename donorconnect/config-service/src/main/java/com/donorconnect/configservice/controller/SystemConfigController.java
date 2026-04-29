package com.donorconnect.configservice.controller;

import com.donorconnect.configservice.entity.SystemConfig;
import com.donorconnect.configservice.service.SystemConfigService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController 
@RequestMapping("/configs") 
@RequiredArgsConstructor
public class SystemConfigController {
    private final SystemConfigService service;

    @GetMapping 
    public ResponseEntity<List<SystemConfig>> all() { 
        return ResponseEntity.ok(service.getAll()); 
    }

    @GetMapping("/{key}") 
    public ResponseEntity<SystemConfig> byKey(@PathVariable String key) {
        return service.getByKey(key).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping 
    public ResponseEntity<SystemConfig> save(@RequestBody SystemConfig config) { 
        return ResponseEntity.ok(service.save(config)); 
    }

    @DeleteMapping("/{id}") 
    public ResponseEntity<Void> delete(@PathVariable Long id) { 
        service.delete(id); return ResponseEntity.ok().build(); 
    }
}