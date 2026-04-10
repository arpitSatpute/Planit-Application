package com.planit.controller;

import com.planit.dto.request.LoginRequest;
import com.planit.dto.request.RegisterRequest;
import com.planit.dto.request.SendOtpRequest;
import com.planit.dto.request.VerifyOtpRequest;
import com.planit.dto.response.ApiResponse;
import com.planit.dto.response.AuthResponse;
import com.planit.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Registration successful"));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<Map<String, Object>>> refreshToken(
            @RequestBody Map<String, String> request) {
        String newToken = authService.refreshAccessToken(request.get("refreshToken"));
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("accessToken", newToken, "expiresIn", 86400L)));
    }

    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponse<Map<String, Object>>> sendOtp(
            @Valid @RequestBody SendOtpRequest request) {
        String otpId = authService.sendOtp(request.getPhone(), request.getPurpose());
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("otpId", otpId, "expiresIn", 300, "message", "OTP sent")));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request) {
        boolean verified = authService.verifyOtp(request.getOtpId(), request.getOtp());
        return ResponseEntity.ok(ApiResponse.success(Map.of("verified", verified)));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader("Authorization") String authHeader) {
        authService.logout(authHeader.replace("Bearer ", ""));
        return ResponseEntity.ok(ApiResponse.success(null, "Logged out successfully"));
    }
}
