package com.taskmanager.taskmanager.controller;

import com.taskmanager.taskmanager.dto.LoginRequest;
import com.taskmanager.taskmanager.dto.LoginResponse;
import com.taskmanager.taskmanager.dto.RegisterRequest;
import com.taskmanager.taskmanager.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // ─── POST /auth/register ──────────────────────────────────────────
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        String message = authService.register(request);
        return ResponseEntity.ok(message);
    }

    // ─── POST /auth/login ─────────────────────────────────────────────
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}