package com.duyanhnguyen.petworld.backend.configuration;

import com.duyanhnguyen.petworld.backend.enums.TokenType;
import com.duyanhnguyen.petworld.backend.service.impl.token.RedisTokenService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CustomJwtValidator implements OAuth2TokenValidator<Jwt> {

    RedisTokenService redisTokenService;

    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {
        if (redisTokenService.isInvalidated(token.getId()))
            return OAuth2TokenValidatorResult.failure(
                    new OAuth2Error("invalid_token", "Token invalidated", null)
            );

        if (!TokenType.ACCESS.name().equals(token.getClaimAsString("token_type")))
            return OAuth2TokenValidatorResult.failure(
                    new OAuth2Error("invalid_token", "Not an access token", null)
            );

        return OAuth2TokenValidatorResult.success();
    }

}
