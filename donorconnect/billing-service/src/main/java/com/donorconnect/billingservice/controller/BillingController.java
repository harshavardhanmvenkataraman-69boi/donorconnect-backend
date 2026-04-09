package com.donorconnect.billingservice.controller;
import com.donorconnect.billingservice.entity.BillingRef;
import com.donorconnect.billingservice.service.BillingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/billing") @RequiredArgsConstructor
public class BillingController {
    private final BillingService service;
    @GetMapping public ResponseEntity<List<BillingRef>> all() { return ResponseEntity.ok(service.getAll()); }
    @GetMapping("/issue/{issueId}") public ResponseEntity<List<BillingRef>> byIssue(@PathVariable Long issueId) { return ResponseEntity.ok(service.getByIssueId(issueId)); }
    @PostMapping public ResponseEntity<BillingRef> create(@RequestBody BillingRef ref) { return ResponseEntity.ok(service.save(ref)); }
}
