package com.co.ucentral.gestionResiduos.back.auth;


import com.co.ucentral.gestionResiduos.back.auth.login.LoginRequest;
import com.co.ucentral.gestionResiduos.back.auth.register.*;
import com.co.ucentral.gestionResiduos.back.auth.resetPassword.PasswordResetConfirmRequest;
import com.co.ucentral.gestionResiduos.back.auth.resetPassword.PasswordResetRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;

import java.util.Map;

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
    @PostMapping("/register/google")
    public ResponseEntity<AuthResponse> registerGoogle(
            @RequestBody GoogleRegisterRequest request) {
        return ResponseEntity.ok(authService.registerGoogle(request));
    }
    @PostMapping("/login/google")
    public ResponseEntity<AuthResponse> loginGoogle(
            @RequestBody Map<String, String> request) {
        return ResponseEntity.ok(authService.loginGoogle(
                request.get("email"),
                request.get("googleId")
        ));
    }
    @PostMapping("/forgot-password")
    public ResponseEntity<RegisterResponse> forgotPassword(
            @RequestBody PasswordResetRequest request) {
        return ResponseEntity.ok(authService.requestPasswordReset(request.getEmail()));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<RegisterResponse> resetPassword(
            @RequestBody PasswordResetConfirmRequest request) {
        return ResponseEntity.ok(authService.resetPassword(
                request.getToken(),
                request.getNewPassword()
        ));
    }
}
