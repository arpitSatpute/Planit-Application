package com.planit.service.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Primary
@Component
@RequiredArgsConstructor
public class ResilientAuthStateStore implements AuthStateStore {

    private final ObjectProvider<RedisAuthStateStore> redisStoreProvider;
    private final InMemoryAuthStateStore inMemoryStore;

    @Value("${app.auth.store.prefer-redis:true}")
    private boolean preferRedis;

    @Value("${app.auth.store.fallback-enabled:false}")
    private boolean fallbackEnabled;

    @Override
    public void saveOtp(String otpId, OtpData data, Duration ttl) {
        if (tryRedis(store -> {
            store.saveOtp(otpId, data, ttl);
            return true;
        })) {
            return;
        }
        inMemoryStore.saveOtp(otpId, data, ttl);
    }

    @Override
    public Optional<OtpData> findOtp(String otpId) {
        Optional<OtpData> redisResult = tryRedis(store -> store.findOtp(otpId), Optional.empty());
        if (redisResult.isPresent()) {
            return redisResult;
        }
        return inMemoryStore.findOtp(otpId);
    }

    @Override
    public void deleteOtp(String otpId) {
        tryRedis(store -> {
            store.deleteOtp(otpId);
            return true;
        });
        inMemoryStore.deleteOtp(otpId);
    }

    @Override
    public void blacklistToken(String token, Duration ttl) {
        if (tryRedis(store -> {
            store.blacklistToken(token, ttl);
            return true;
        })) {
            return;
        }
        inMemoryStore.blacklistToken(token, ttl);
    }

    @Override
    public boolean isTokenBlacklisted(String token) {
        boolean blacklistedInRedis = tryRedis(store -> store.isTokenBlacklisted(token), false);
        return blacklistedInRedis || inMemoryStore.isTokenBlacklisted(token);
    }

    private boolean tryRedis(RedisWriteOperation operation) {
        return tryRedis(store -> operation.apply(store), false);
    }

    private <T> T tryRedis(RedisReadOperation<T> operation, T fallbackValue) {
        RedisAuthStateStore redisStore = redisStoreProvider.getIfAvailable();
        if (redisStore == null || !preferRedis) {
            return fallbackValue;
        }

        try {
            return operation.apply(redisStore);
        } catch (Exception ex) {
            if (!fallbackEnabled) {
                throw ex;
            }
            log.warn("Redis unavailable for auth state; using in-memory fallback: {}", ex.getMessage());
            return fallbackValue;
        }
    }

    @FunctionalInterface
    private interface RedisWriteOperation {
        boolean apply(RedisAuthStateStore store);
    }

    @FunctionalInterface
    private interface RedisReadOperation<T> {
        T apply(RedisAuthStateStore store);
    }
}

