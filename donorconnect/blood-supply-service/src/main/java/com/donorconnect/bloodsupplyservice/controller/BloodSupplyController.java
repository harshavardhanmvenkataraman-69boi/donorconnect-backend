package com.donorconnect.bloodsupplyservice.controller;

import com.donorconnect.bloodsupplyservice.entity.*;
import com.donorconnect.bloodsupplyservice.service.BloodSupplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/blood") @RequiredArgsConstructor
public class BloodSupplyController {
    private final BloodSupplyService service;

    @PostMapping("/donations") public ResponseEntity<Donation> addDonation(@RequestBody Donation d) { return ResponseEntity.ok(service.saveDonation(d)); }
    @PostMapping("/components") public ResponseEntity<Component> addComponent(@RequestBody Component c) { return ResponseEntity.ok(service.saveComponent(c)); }
    @GetMapping("/components/{id}") public ResponseEntity<Component> getComponent(@PathVariable Long id) { return ResponseEntity.ok(service.getComponentById(id)); }
    @GetMapping("/components/available") public ResponseEntity<List<Component>> available() { return ResponseEntity.ok(service.getAvailableComponents()); }
    @PostMapping("/test-results") public ResponseEntity<TestResult> recordTest(@RequestBody TestResult tr) { return ResponseEntity.ok(service.recordTestResult(tr)); }
    @GetMapping("/inventory") public ResponseEntity<List<InventoryBalance>> inventory() { return ResponseEntity.ok(service.getAllInventory()); }
}
