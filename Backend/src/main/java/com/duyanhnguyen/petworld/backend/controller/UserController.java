package com.duyanhnguyen.petworld.backend.controller;

import com.duyanhnguyen.petworld.backend.dto.request.UserRegistrationRequest;
import com.duyanhnguyen.petworld.backend.dto.response.ApiResponse;
import com.duyanhnguyen.petworld.backend.dto.response.UserResponse;
import com.duyanhnguyen.petworld.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {

    UserService userService;

    @PostMapping("/register")
    public ApiResponse<UserResponse> register(@RequestBody @Valid UserRegistrationRequest request) {
        return ApiResponse.<UserResponse>builder()
                .message("User registered successfully")
                .data(userService.register(request))
                .build();
    }

    @GetMapping("/{userId}")
    public ApiResponse<UserResponse> getUserById(@PathVariable Long userId) {
        return ApiResponse.<UserResponse>builder()
                .message("User retrieved successfully")
                .data(userService.getById(userId))
                .build();
    }

}
