package com.planit.service.auth;

import java.time.Duration;
import java.util.Optional;

public interface AuthStateStore {

    record OtpData(String otp, String phone, String purpose) {}

    void saveOtp(String otpId, OtpData data, Duration ttl);

    Optional<OtpData> findOtp(String otpId);

    void deleteOtp(String otpId);

    void blacklistToken(String token, Duration ttl);

    boolean isTokenBlacklisted(String token);
}

