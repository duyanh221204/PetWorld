package com.duyanhnguyen.petworld.backend.controller.rest;

import com.duyanhnguyen.petworld.backend.dto.request.UserRegistrationRequest;
import com.duyanhnguyen.petworld.backend.dto.response.ApiResponse;
import com.duyanhnguyen.petworld.backend.dto.response.FriendshipStatusResponse;
import com.duyanhnguyen.petworld.backend.dto.response.UserResponse;
import com.duyanhnguyen.petworld.backend.service.FriendshipService;
import com.duyanhnguyen.petworld.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {

    UserService userService;
    FriendshipService friendshipService;

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

    @GetMapping("/{userId}/friends-list")
    public ApiResponse<Page<UserResponse>> getFriendsList(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "100") Integer size
    ) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        return ApiResponse.<Page<UserResponse>>builder()
                .message("User's friends list retrieved successfully")
                .data(friendshipService.getFriendsList(userId, pageable))
                .build();
    }

    @GetMapping("/{userId}/friendship-status")
    public ApiResponse<FriendshipStatusResponse> getFriendshipStatus(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long userId
    ) {
        Long currentUserId = Long.parseLong(jwt.getSubject());
        return ApiResponse.<FriendshipStatusResponse>builder()
                .message("Friendship status retrieved successfully")
                .data(friendshipService.getFriendshipStatus(currentUserId, userId))
                .build();
    }

    @GetMapping("/search")
    public ApiResponse<Page<UserResponse>> searchByUsername(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "100") Integer size
    ) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        return ApiResponse.<Page<UserResponse>>builder()
                .message("Users retrieved successfully")
                .data(userService.searchByUsername(keyword, pageable))
                .build();
    }

}
