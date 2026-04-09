package com.donorconnect.authservice.controller;

import com.donorconnect.authservice.dto.UserDto;
import com.donorconnect.authservice.entity.User;
import com.donorconnect.authservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/users") @RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    @GetMapping public ResponseEntity<List<UserDto>> all() { return ResponseEntity.ok(userService.getAllUsers()); }
    @GetMapping("/{id}") public ResponseEntity<UserDto> get(@PathVariable Long id) { return ResponseEntity.ok(userService.getUserById(id)); }
    @PostMapping public ResponseEntity<UserDto> create(@RequestBody User user) { return ResponseEntity.ok(userService.createUser(user)); }
    @PutMapping("/{id}/lock") public ResponseEntity<UserDto> lock(@PathVariable Long id) { return ResponseEntity.ok(userService.lockUser(id)); }
    @PutMapping("/{id}/unlock") public ResponseEntity<UserDto> unlock(@PathVariable Long id) { return ResponseEntity.ok(userService.unlockUser(id)); }
}
