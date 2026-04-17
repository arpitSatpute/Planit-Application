package com.planit.service.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RedisAuthStateStore implements AuthStateStore {

    private static final String OTP_PREFIX = "otp:";
    private static final String BLACKLIST_PREFIX = "blacklist:";

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void saveOtp(String otpId, OtpData data, Duration ttl) {
        String key = OTP_PREFIX + otpId;
        redisTemplate.opsForHash().put(key, "otp", data.otp());
        redisTemplate.opsForHash().put(key, "phone", data.phone());
        redisTemplate.opsForHash().put(key, "purpose", data.purpose());
        redisTemplate.expire(key, ttl);
    }

    @Override
    public Optional<OtpData> findOtp(String otpId) {
        String key = OTP_PREFIX + otpId;
        Object otp = redisTemplate.opsForHash().get(key, "otp");
        if (otp == null) {
            return Optional.empty();
        }
        Object phone = redisTemplate.opsForHash().get(key, "phone");
        Object purpose = redisTemplate.opsForHash().get(key, "purpose");
        return Optional.of(new OtpData(
                String.valueOf(otp),
                phone == null ? null : String.valueOf(phone),
                purpose == null ? null : String.valueOf(purpose)
        ));
    }

    @Override
    public void deleteOtp(String otpId) {
        redisTemplate.delete(OTP_PREFIX + otpId);
    }

    @Override
    public void blacklistToken(String token, Duration ttl) {
        redisTemplate.opsForValue().set(BLACKLIST_PREFIX + token, "1", ttl);
    }

    @Override
    public boolean isTokenBlacklisted(String token) {
        Boolean exists = redisTemplate.hasKey(BLACKLIST_PREFIX + token);
        return exists != null && exists;
    }
}

