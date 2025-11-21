package org.example.backend.service;


import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.backend.dto.request.UserRegistrationRequest;
import org.example.backend.dto.response.UserResponse;
import org.example.backend.entity.UserEntity;
import org.example.backend.mapper.UserMapper;
import org.example.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {

    UserRepository userRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;

    public UserResponse register(UserRegistrationRequest userRegistrationRequest) {
        UserEntity userEntity = userMapper.toEntity(userRegistrationRequest);
        userEntity.setHashedPassword(passwordEncoder.encode(userRegistrationRequest.getPassword()));
        return userMapper.toResponse(userRepository.save(userEntity));
    }

}
