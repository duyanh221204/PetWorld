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

    public void save(String jwtId, Date expirationTime) {
        long timeToLive = (expirationTime.getTime() - new Date().getTime()) / 1000;
        stringRedisTemplate.opsForValue().set(getKey(jwtId), "", timeToLive);
    }

    public boolean isInvalidated(String jwtId) {
        return stringRedisTemplate.hasKey(getKey(jwtId));
    }

    private String getKey(String jwtId) {
        return "jwt:invalidated:" + jwtId;
    }

}
