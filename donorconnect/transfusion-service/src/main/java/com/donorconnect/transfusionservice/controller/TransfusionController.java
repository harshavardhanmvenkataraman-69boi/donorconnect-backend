package com.donorconnect.transfusionservice.controller;

import com.donorconnect.transfusionservice.entity.*;
import com.donorconnect.transfusionservice.service.TransfusionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/transfusion") @RequiredArgsConstructor
public class TransfusionController {
    private final TransfusionService service;

    @PostMapping("/crossmatch-requests") public ResponseEntity<CrossmatchRequest> createRequest(@RequestBody CrossmatchRequest req) { return ResponseEntity.ok(service.createRequest(req)); }
    @GetMapping("/crossmatch-requests/pending") public ResponseEntity<List<CrossmatchRequest>> pending() { return ResponseEntity.ok(service.getPendingRequests()); }
    @PostMapping("/crossmatch-results") public ResponseEntity<CrossmatchResult> confirm(@RequestBody CrossmatchResult result) { return ResponseEntity.ok(service.confirmCrossmatch(result)); }
    @PostMapping("/issues") public ResponseEntity<IssueRecord> issue(@RequestBody IssueRecord rec) { return ResponseEntity.ok(service.issueComponent(rec)); }
    @GetMapping("/issues/patient/{id}") public ResponseEntity<List<IssueRecord>> byPatient(@PathVariable Long id) { return ResponseEntity.ok(service.getIssuesByPatient(id)); }
}
