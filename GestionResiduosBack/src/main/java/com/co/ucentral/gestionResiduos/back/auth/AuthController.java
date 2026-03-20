package com.co.ucentral.gestionResiduos.back.auth;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor

public class AuthController {

    private final AuthService authService;

    @PostMapping("/register/user")
    public ResponseEntity<RegisterResponse> register(
            @RequestBody @Valid RegisterRequest request) {

        return ResponseEntity.ok(authService.register(request));
    }

    @GetMapping("/verify")
    public ResponseEntity<AuthResponse> verificar(@RequestParam String token) {
        return ResponseEntity.ok(authService.verify(token));
    }


    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestBody @Valid LoginRequest request) {

        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @RequestBody String refreshToken) {

        return ResponseEntity.ok(authService.refresh(refreshToken));
    }


    @PostMapping("/resend-verification")
    public ResponseEntity<RegisterResponse> resendVerification(
            @RequestBody ResendVerificationRequest request) {

        return ResponseEntity.ok(authService.resendVerificationEmail(request.getEmail()));
    }

    @PutMapping("/update-profile")
    public ResponseEntity<RegisterResponse> updateProfile(
            @RequestBody @Valid UpdateProfileRequest request) {

        return ResponseEntity.ok(authService.updateProfile(request));
    }

}
