package com.duyanhnguyen.petworld.backend.service.impl.token;

import com.duyanhnguyen.petworld.backend.dto.response.TokenResponse;
import com.duyanhnguyen.petworld.backend.entity.UserEntity;
import com.duyanhnguyen.petworld.backend.enums.ErrorCode;
import com.duyanhnguyen.petworld.backend.enums.TokenType;
import com.duyanhnguyen.petworld.backend.exception.AppException;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JwtService {

    RedisTokenService redisTokenService;

    @NonFinal
    @Value("${jwt.issuer}")
    String issuer;

    @NonFinal
    @Value("${jwt.signer-key}")
    String signerKey;

    public TokenResponse generateAccessToken(UserEntity userEntity) {
        return generateToken(userEntity, true);
    }

    public TokenResponse generateRefreshToken(UserEntity userEntity) {
        return generateToken(userEntity, false);
    }

    private TokenResponse generateToken(UserEntity userEntity, boolean isAccessToken) {
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(Long.toString(userEntity.getId()))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", userEntity.getRole().name())
                .claim("token_type", isAccessToken ? TokenType.ACCESS.name() : TokenType.REFRESH.name())
                .issuer(issuer)
                .issueTime(new Date())
                .expirationTime(
                        new Date(
                                Instant.now().plus(
                                        isAccessToken ? 15 : 7,
                                        isAccessToken ? ChronoUnit.MINUTES : ChronoUnit.DAYS
                                ).toEpochMilli()
                        )
                )
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(jwsHeader, payload);
        try {
            jwsObject.sign(new MACSigner(signerKey.getBytes()));
            return TokenResponse.builder()
                    .token(jwsObject.serialize())
                    .jwtId(jwtClaimsSet.getJWTID())
                    .expirationTime(jwtClaimsSet.getExpirationTime())
                    .build();
        } catch (JOSEException e) {
            log.error("Error generating token: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public SignedJWT verifyToken(String token) throws JOSEException, ParseException {
        JWSVerifier jwsVerifier = new MACVerifier(signerKey.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);
        String jwtId = signedJWT.getJWTClaimsSet().getJWTID();
        Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        if (!signedJWT.verify(jwsVerifier) || expirationTime.before(new Date()) || redisTokenService.isInvalidated(jwtId))
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        return signedJWT;
    }

}
