package com.duyanhnguyen.petworld.backend.service;

import com.duyanhnguyen.petworld.backend.dto.request.UserRegistrationRequest;
import com.duyanhnguyen.petworld.backend.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    UserResponse register(UserRegistrationRequest userRegistrationRequest);

    UserResponse getById(Long userId);

    Page<UserResponse> searchByUsername(String keyword, Pageable pageable);

}
