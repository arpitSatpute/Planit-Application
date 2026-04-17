package com.planit.service.auth;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryAuthStateStore implements AuthStateStore {

    private record OtpEntry(OtpData data, long expiresAtEpochMs) {}

    private record TokenEntry(long expiresAtEpochMs) {}

    private final Map<String, OtpEntry> otpStore = new ConcurrentHashMap<>();
    private final Map<String, TokenEntry> tokenBlacklist = new ConcurrentHashMap<>();

    @Override
    public void saveOtp(String otpId, OtpData data, Duration ttl) {
        otpStore.put(otpId, new OtpEntry(data, now() + ttl.toMillis()));
    }

    @Override
    public Optional<OtpData> findOtp(String otpId) {
        OtpEntry entry = otpStore.get(otpId);
        if (entry == null) {
            return Optional.empty();
        }
        if (entry.expiresAtEpochMs() <= now()) {
            otpStore.remove(otpId);
            return Optional.empty();
        }
        return Optional.of(entry.data());
    }

    @Override
    public void deleteOtp(String otpId) {
        otpStore.remove(otpId);
    }

    @Override
    public void blacklistToken(String token, Duration ttl) {
        tokenBlacklist.put(token, new TokenEntry(now() + ttl.toMillis()));
    }

    @Override
    public boolean isTokenBlacklisted(String token) {
        TokenEntry entry = tokenBlacklist.get(token);
        if (entry == null) {
            return false;
        }
        if (entry.expiresAtEpochMs() <= now()) {
            tokenBlacklist.remove(token);
            return false;
        }
        return true;
    }

    private long now() {
        return System.currentTimeMillis();
    }
}

