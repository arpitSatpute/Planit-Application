package com.planit.service;

import com.planit.dto.request.LoginRequest;
import com.planit.dto.request.RegisterRequest;
import com.planit.dto.response.AuthResponse;
import com.planit.exception.ResourceNotFoundException;
import com.planit.exception.UnauthorizedException;
import com.planit.exception.ValidationException;
import com.planit.model.User;
import com.planit.repository.UserRepository;
import com.planit.security.JwtTokenProvider;
import com.planit.service.auth.AuthStateStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthStateStore authStateStore;

    public AuthResponse register(RegisterRequest request) {
        // Check duplicate email/phone
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("Email is already registered");
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new ValidationException("Phone number is already registered");
        }

        User.UserRole role;
        try {
            role = User.UserRole.valueOf(request.getRole());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid role: " + request.getRole());
        }

        User user = User.builder()
                .email(request.getEmail())
                .phone(request.getPhone())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .profile(User.Profile.builder()
                        .firstName(request.getFirstName())
                        .lastName(request.getLastName())
                        .build())
                .preferences(User.Preferences.builder()
                        .language("en")
                        .currency("INR")
                        .notificationSettings(new User.Preferences.NotificationSettings(true, true, true, false))
                        .build())
                .status(User.UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        user = userRepository.save(user);
        log.info("New user registered: {} ({})", user.getId(), user.getRole());

        return buildAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        // Support login with email or phone
        User user = userRepository.findByEmailOrPhone(request.getIdentifier(), request.getIdentifier())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        if (user.getStatus() == User.UserStatus.SUSPENDED) {
            throw new UnauthorizedException("Account is suspended. Contact support.");
        }

        log.info("User logged in: {}", user.getId());
        return buildAuthResponse(user);
    }

    public String refreshAccessToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }
        String userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        String roleStr = jwtTokenProvider.getRoleFromToken(refreshToken);
        User.UserRole role = User.UserRole.valueOf(roleStr);
        return jwtTokenProvider.generateAccessToken(userId, role);
    }

    public String sendOtp(String phone, String purpose) {
        // Generate 6 digit OTP
        String otp = String.format("%06d", (int) (Math.random() * 1_000_000));
        String otpId = UUID.randomUUID().toString();

        // Store OTP with 5-minute TTL (Redis or local in-memory fallback)
        authStateStore.saveOtp(otpId, new AuthStateStore.OtpData(otp, phone, purpose), Duration.ofMinutes(5));

        // In production: send via Twilio
        log.info("OTP {} generated for phone {} (purpose: {})", otp, phone, purpose);

        return otpId;
    }

    public boolean verifyOtp(String otpId, String otp) {
        AuthStateStore.OtpData stored = authStateStore.findOtp(otpId)
                .orElse(null);

        if (stored == null) {
            throw new ValidationException("OTP has expired or is invalid");
        }
        boolean valid = stored.otp().equals(otp);
        if (valid) {
            authStateStore.deleteOtp(otpId);
        }
        return valid;
    }

    public void logout(String token) {
        if (jwtTokenProvider.validateToken(token)) {
            long ttl = jwtTokenProvider.getExpirationFromToken(token).getTime() - System.currentTimeMillis();
            if (ttl > 0) {
                authStateStore.blacklistToken(token, Duration.ofMillis(ttl));
            }
        }
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getRole());

        String firstName = user.getProfile() != null ? user.getProfile().getFirstName() : "";
        String lastName = user.getProfile() != null ? user.getProfile().getLastName() : "";
        String avatar = user.getProfile() != null ? user.getProfile().getAvatar() : null;

        return AuthResponse.builder()
                .userId(user.getId())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(86400L)
                .user(AuthResponse.UserSummary.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .phone(user.getPhone())
                        .role(user.getRole().name())
                        .firstName(firstName)
                        .lastName(lastName)
                        .avatar(avatar)
                        .build())
                .build();
    }
}
