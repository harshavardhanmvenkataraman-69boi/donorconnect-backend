package com.donorconnect.safetyservice.controller;

import com.donorconnect.safetyservice.entity.*;
import com.donorconnect.safetyservice.enums.ReactionStatus;
import com.donorconnect.safetyservice.service.SafetyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/safety") @RequiredArgsConstructor
public class SafetyController {
    private final SafetyService service;

    @GetMapping("/reactions") public ResponseEntity<List<Reaction>> all() { return ResponseEntity.ok(service.getAllReactions()); }
    @GetMapping("/reactions/patient/{id}") public ResponseEntity<List<Reaction>> byPatient(@PathVariable Long id) { return ResponseEntity.ok(service.getReactionsByPatient(id)); }
    @PostMapping("/reactions") public ResponseEntity<Reaction> log(@RequestBody Reaction r) { return ResponseEntity.ok(service.logReaction(r)); }
    @PutMapping("/reactions/{id}/status") public ResponseEntity<Reaction> updateStatus(@PathVariable Long id, @RequestParam ReactionStatus status) { return ResponseEntity.ok(service.updateReactionStatus(id, status)); }
    @GetMapping("/lookback/{donationId}") public ResponseEntity<List<LookbackTrace>> lookback(@PathVariable Long donationId) { return ResponseEntity.ok(service.getTracesByDonation(donationId)); }
    @PostMapping("/lookback") public ResponseEntity<LookbackTrace> trace(@RequestBody LookbackTrace trace) { return ResponseEntity.ok(service.saveTrace(trace)); }
}
