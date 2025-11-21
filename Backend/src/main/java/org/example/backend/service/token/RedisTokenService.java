package org.example.backend.service.token;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RedisTokenService {

    StringRedisTemplate stringRedisTemplate;

    public void saveInvalidatedToken(String jwtId, Date expirationTime) {
        long timeToLive = (expirationTime.getTime() - new Date().getTime()) / 1000;
        stringRedisTemplate.opsForValue().set(getInvalidatedTokenKey(jwtId), "", timeToLive);
    }

    public boolean isInvalidated(String jwtId) {
        return stringRedisTemplate.hasKey(getInvalidatedTokenKey(jwtId));
    }

    public void saveRefreshToken(String jwtId, String refreshToken, Date expirationTime) {
        long timeToLive = (expirationTime.getTime() - new Date().getTime()) / 1000;
        stringRedisTemplate.opsForValue().set(getRefreshTokenKey(jwtId), refreshToken, timeToLive);
    }

    public boolean isRefreshToken(String jwtId) {
        return stringRedisTemplate.hasKey(getRefreshTokenKey(jwtId));
    }

    private String getInvalidatedTokenKey(String jwtId) {
        return "jwt:invalidated:" + jwtId;
    }

    private String getRefreshTokenKey(String jwtId) {
        return "jwt:refresh:" + jwtId;
    }

}
