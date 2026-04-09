package com.donorconnect.donorservice.controller;

import com.donorconnect.donorservice.entity.*;
import com.donorconnect.donorservice.service.DonorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/donors") @RequiredArgsConstructor
public class DonorController {
    private final DonorService donorService;

    @GetMapping public ResponseEntity<List<Donor>> all() { return ResponseEntity.ok(donorService.getAllDonors()); }
    @GetMapping("/{id}") public ResponseEntity<Donor> get(@PathVariable Long id) { return ResponseEntity.ok(donorService.getDonorById(id)); }
    @PostMapping public ResponseEntity<Donor> register(@RequestBody Donor donor) { return ResponseEntity.ok(donorService.registerDonor(donor)); }
    @PostMapping("/{id}/defer") public ResponseEntity<Deferral> defer(@PathVariable Long id, @RequestBody Deferral deferral) { return ResponseEntity.ok(donorService.deferDonor(id, deferral)); }
    @GetMapping("/{id}/deferrals") public ResponseEntity<List<Deferral>> deferrals(@PathVariable Long id) { return ResponseEntity.ok(donorService.getDeferralsByDonor(id)); }
    @GetMapping("/{id}/screenings") public ResponseEntity<List<ScreeningRecord>> screenings(@PathVariable Long id) { return ResponseEntity.ok(donorService.getScreeningsByDonor(id)); }
    @PostMapping("/{id}/screenings") public ResponseEntity<ScreeningRecord> screen(@PathVariable Long id, @RequestBody ScreeningRecord rec) { rec.setDonorId(id); return ResponseEntity.ok(donorService.saveScreening(rec)); }
}
