package org.example.backend.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.backend.dto.request.AuthenticationRequest;
import org.example.backend.dto.request.UserActivationRequest;
import org.example.backend.dto.response.AuthenticationResponse;
import org.example.backend.entity.UserEntity;
import org.example.backend.enums.ErrorCode;
import org.example.backend.exception.AppException;
import org.example.backend.repository.UserRepository;
import org.example.backend.service.token.JwtService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {

    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    JwtService jwtService;
    StringRedisTemplate stringRedisTemplate;

    public AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest) {
        UserEntity userEntity = userRepository.findByUsername(authenticationRequest.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.LOGIN_FAILED));

        if (!passwordEncoder.matches(authenticationRequest.getPassword(), userEntity.getHashedPassword()))
            throw new AppException(ErrorCode.LOGIN_FAILED);

        return AuthenticationResponse.builder()
                .accessToken(jwtService.generateAccessToken(userEntity))
                .refreshToken(jwtService.generateRefreshToken(userEntity))
                .build();
    }

    public void activateUser(UserActivationRequest userActivationRequest) {
        UserEntity userEntity = userRepository.findByEmail(userActivationRequest.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_ACTIVATION_FAILED));

        String storedCode = stringRedisTemplate.opsForValue().get("activation:" + userActivationRequest.getEmail());
        if (storedCode == null || !storedCode.equals(userActivationRequest.getVerificationCode()))
            throw new AppException(ErrorCode.USER_ACTIVATION_FAILED);

        userEntity.setIsActive(true);
        userRepository.save(userEntity);
        stringRedisTemplate.delete("activation:" + userActivationRequest.getEmail());
    }

}
