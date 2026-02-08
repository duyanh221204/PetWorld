package com.duyanhnguyen.petworld.backend.service.impl.token;

import com.duyanhnguyen.petworld.backend.utils.RedisKeyGenerator;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RedisTokenService {

    StringRedisTemplate stringRedisTemplate;

    public void saveInvalidatedToken(String jwtId, Date expirationTime) {
        long timeToLive = (expirationTime.getTime() - System.currentTimeMillis()) / 1000;
        stringRedisTemplate.opsForValue().set(RedisKeyGenerator.getInvalidatedTokenKey(jwtId), "", Duration.ofSeconds(timeToLive));
    }

    public boolean isInvalidated(String jwtId) {
        return stringRedisTemplate.hasKey(RedisKeyGenerator.getInvalidatedTokenKey(jwtId));
    }

    public void saveValidatedRefreshToken(String jwtId, Date expirationTime) {
        long timeToLive = (expirationTime.getTime() - System.currentTimeMillis()) / 1000;
        stringRedisTemplate.opsForValue().set(RedisKeyGenerator.getRefreshTokenKey(jwtId), "", Duration.ofSeconds(timeToLive));
    }

    public boolean isRefreshTokenValidated(String jwtId) {
        return stringRedisTemplate.hasKey(RedisKeyGenerator.getRefreshTokenKey(jwtId));
    }

    public void deleteRefreshToken(String jwtId) {
        stringRedisTemplate.delete(RedisKeyGenerator.getRefreshTokenKey(jwtId));
    }

}
