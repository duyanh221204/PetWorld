package com.duyanhnguyen.petworld.backend.service.impl;

import com.duyanhnguyen.petworld.backend.dto.request.UserRegistrationRequest;
import com.duyanhnguyen.petworld.backend.dto.response.UserResponse;
import com.duyanhnguyen.petworld.backend.elasticsearch.service.ESUserService;
import com.duyanhnguyen.petworld.backend.entity.UserEntity;
import com.duyanhnguyen.petworld.backend.enums.ErrorCode;
import com.duyanhnguyen.petworld.backend.exception.AppException;
import com.duyanhnguyen.petworld.backend.mapper.UserMapper;
import com.duyanhnguyen.petworld.backend.repository.FriendshipRepository;
import com.duyanhnguyen.petworld.backend.repository.PostRepository;
import com.duyanhnguyen.petworld.backend.repository.UserRepository;
import com.duyanhnguyen.petworld.backend.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements UserService {

    UserRepository userRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    FriendshipRepository friendshipRepository;
    PostRepository postRepository;
    ESUserService esUserService;

    @Override
    public UserResponse register(UserRegistrationRequest userRegistrationRequest) {
        if (userRepository.existsByUsername(userRegistrationRequest.getUsername()))
            throw new AppException(ErrorCode.USERNAME_ALREADY_EXISTED);
        if (userRepository.existsByEmail(userRegistrationRequest.getEmail()))
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTED);

        UserEntity userEntity = userMapper.toEntity(userRegistrationRequest);
        userEntity.setHashedPassword(passwordEncoder.encode(userRegistrationRequest.getPassword()));

        UserResponse userResponse = userMapper.toResponse(userRepository.save(userEntity));
        userResponse.setFriendCount(0L);
        userResponse.setPostCount(0L);
        return userResponse;
    }

    @Override
    public UserResponse getById(Long userId) {
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        UserResponse userResponse = userMapper.toResponse(userEntity);
        userResponse.setFriendCount(friendshipRepository.countByUserIdAndAcceptedAtIsNotNull(userId));
        userResponse.setPostCount(postRepository.countByCreatorId(userId));
        return userResponse;
    }

    @Override
    public Page<UserResponse> searchByUsername(String keyword, Pageable pageable) {
        Page<Long> userIds = esUserService.searchByKeyword(keyword, pageable);
        List<UserEntity> users = userRepository.findAllByIdIn(userIds.getContent());

        Map<Long, UserEntity> usersMap = users.stream()
                .collect(Collectors.toMap(UserEntity::getId, Function.identity()));
        users = userIds.getContent().stream()
                .map(usersMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        List<UserResponse> userResponses = userMapper.toResponseList(users);
        return new PageImpl<>(userResponses, pageable, userIds.getTotalElements());
    }

}
