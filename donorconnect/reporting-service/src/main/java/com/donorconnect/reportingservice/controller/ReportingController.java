package com.donorconnect.reportingservice.controller;
import com.donorconnect.reportingservice.entity.LabReportPack;
import com.donorconnect.reportingservice.enums.ReportScope;
import com.donorconnect.reportingservice.service.ReportingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/reports") @RequiredArgsConstructor
public class ReportingController {
    private final ReportingService service;
    @GetMapping public ResponseEntity<List<LabReportPack>> all() { return ResponseEntity.ok(service.getAll()); }
    @GetMapping("/scope/{scope}") public ResponseEntity<List<LabReportPack>> byScope(@PathVariable ReportScope scope) { return ResponseEntity.ok(service.getByScope(scope)); }
    @PostMapping public ResponseEntity<LabReportPack> create(@RequestBody LabReportPack pack) { return ResponseEntity.ok(service.save(pack)); }
}
