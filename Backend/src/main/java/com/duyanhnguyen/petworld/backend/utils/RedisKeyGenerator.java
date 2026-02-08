package com.duyanhnguyen.petworld.backend.utils;

public class RedisKeyGenerator {

    public static String generateVerificationCodeKey(String email) {
        return "verification:" + email;
    }

    public static String getInvalidatedTokenKey(String jwtId) {
        return "jwt:invalidated:" + jwtId;
    }

    public static String getRefreshTokenKey(String jwtId) {
        return "jwt:refresh:" + jwtId;
    }

}
