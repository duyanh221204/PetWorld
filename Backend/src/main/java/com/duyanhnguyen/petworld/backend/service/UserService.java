package com.duyanhnguyen.petworld.backend.service;

import com.duyanhnguyen.petworld.backend.dto.request.UserRegistrationRequest;
import com.duyanhnguyen.petworld.backend.dto.response.UserResponse;

public interface UserService {

    UserResponse register(UserRegistrationRequest userRegistrationRequest);

    UserResponse getById(Long userId);

}
