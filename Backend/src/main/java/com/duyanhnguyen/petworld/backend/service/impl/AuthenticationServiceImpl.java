package com.duyanhnguyen.petworld.backend.service.impl;

import com.duyanhnguyen.petworld.backend.dto.request.AuthenticationRequest;
import com.duyanhnguyen.petworld.backend.dto.request.UserActivationRequest;
import com.duyanhnguyen.petworld.backend.dto.response.AuthenticationResponse;
import com.duyanhnguyen.petworld.backend.dto.response.TokenResponse;
import com.duyanhnguyen.petworld.backend.entity.UserEntity;
import com.duyanhnguyen.petworld.backend.enums.ErrorCode;
import com.duyanhnguyen.petworld.backend.enums.TokenType;
import com.duyanhnguyen.petworld.backend.event.UserEvent;
import com.duyanhnguyen.petworld.backend.exception.AppException;
import com.duyanhnguyen.petworld.backend.repository.UserRepository;
import com.duyanhnguyen.petworld.backend.service.AuthenticationService;
import com.duyanhnguyen.petworld.backend.service.impl.token.JwtService;
import com.duyanhnguyen.petworld.backend.service.impl.token.RedisTokenService;
import com.duyanhnguyen.petworld.backend.utils.RedisKeyGenerator;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.util.Date;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationServiceImpl implements AuthenticationService {

    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    JwtService jwtService;
    StringRedisTemplate stringRedisTemplate;
    RedisTokenService redisTokenService;
    ApplicationEventPublisher applicationEventPublisher;

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest) {
        UserEntity userEntity = userRepository.findByUsernameAndIsActive(authenticationRequest.getUsername(), true)
                .orElseThrow(() -> new AppException(ErrorCode.LOGIN_FAILED));

        if (!passwordEncoder.matches(authenticationRequest.getPassword(), userEntity.getHashedPassword()))
            throw new AppException(ErrorCode.LOGIN_FAILED);

        TokenResponse accessToken = jwtService.generateAccessToken(userEntity);
        TokenResponse refreshToken = jwtService.generateRefreshToken(userEntity);
        redisTokenService.saveValidatedRefreshToken(refreshToken.getJwtId(), refreshToken.getExpirationTime());

        return AuthenticationResponse.builder()
                .accessToken(accessToken.getToken())
                .refreshToken(refreshToken.getToken())
                .user(
                        AuthenticationResponse.User.builder()
                                .id(userEntity.getId())
                                .username(userEntity.getUsername())
                                .avatar(userEntity.getAvatar())
                                .isActive(userEntity.getIsActive())
                                .role(userEntity.getRole())
                                .build()
                )
                .build();
    }

    @Transactional
    @Override
    public void activateUser(UserActivationRequest userActivationRequest) {
        UserEntity userEntity = userRepository.findByEmailAndIsActive(userActivationRequest.getEmail(), false)
                .orElseThrow(() -> new AppException(ErrorCode.USER_ACTIVATION_FAILED));

        String key = RedisKeyGenerator.generateVerificationCodeKey(userActivationRequest.getEmail());
        String storedCode = stringRedisTemplate.opsForValue().get(key);
        if (storedCode == null || !storedCode.equals(userActivationRequest.getVerificationCode())) {
            stringRedisTemplate.delete(key);
            throw new AppException(ErrorCode.USER_ACTIVATION_FAILED);
        }

        userEntity.setIsActive(true);
        applicationEventPublisher.publishEvent(new UserEvent(userEntity.getId()));
        stringRedisTemplate.delete(key);
    }

    @Override
    public AuthenticationResponse refreshToken(String refreshToken) {
        try {
            SignedJWT signedJWT = jwtService.verifyToken(refreshToken);
            String jwtId = signedJWT.getJWTClaimsSet().getJWTID();
            Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
            Long userId = Long.parseLong(signedJWT.getJWTClaimsSet().getSubject());
            boolean isRefreshToken = TokenType.REFRESH.name().equals(signedJWT.getJWTClaimsSet().getStringClaim("token_type"));

            if (!isRefreshToken || redisTokenService.isInvalidated(jwtId) || !redisTokenService.isRefreshTokenValidated(jwtId))
                throw new AppException(ErrorCode.UNAUTHENTICATED);

            redisTokenService.saveInvalidatedToken(jwtId, expirationTime);
            redisTokenService.deleteRefreshToken(jwtId);

            UserEntity userEntity = userRepository.findById(userId)
                    .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

            TokenResponse newAccessToken = jwtService.generateAccessToken(userEntity);
            TokenResponse newRefreshToken = jwtService.generateRefreshToken(userEntity);
            redisTokenService.saveValidatedRefreshToken(newRefreshToken.getJwtId(), newRefreshToken.getExpirationTime());

            return AuthenticationResponse.builder()
                    .accessToken(newAccessToken.getToken())
                    .refreshToken(newRefreshToken.getToken())
                    .user(
                            AuthenticationResponse.User.builder()
                                    .id(userEntity.getId())
                                    .username(userEntity.getUsername())
                                    .avatar(userEntity.getAvatar())
                                    .isActive(userEntity.getIsActive())
                                    .role(userEntity.getRole())
                                    .build()
                    )
                    .build();
        } catch (ParseException | JOSEException e) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
    }

    @Override
    public void logout(String accessToken, String refreshToken) {
        try {
            SignedJWT signedJWT = jwtService.verifyToken(accessToken);
            if (!TokenType.ACCESS.name().equals(signedJWT.getJWTClaimsSet().getStringClaim("token_type")))
                throw new AppException(ErrorCode.UNAUTHENTICATED);
            String jwtId = signedJWT.getJWTClaimsSet().getJWTID();
            Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
            redisTokenService.saveInvalidatedToken(jwtId, expirationTime);

            signedJWT = jwtService.verifyToken(refreshToken);
            if (!TokenType.REFRESH.name().equals(signedJWT.getJWTClaimsSet().getStringClaim("token_type")))
                throw new AppException(ErrorCode.UNAUTHENTICATED);
            jwtId = signedJWT.getJWTClaimsSet().getJWTID();
            expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
            redisTokenService.saveInvalidatedToken(jwtId, expirationTime);
            redisTokenService.deleteRefreshToken(jwtId);
        } catch (ParseException | JOSEException e) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
    }

}
