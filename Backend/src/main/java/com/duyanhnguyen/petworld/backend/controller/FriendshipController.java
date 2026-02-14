package com.duyanhnguyen.petworld.backend.controller;

import com.duyanhnguyen.petworld.backend.dto.response.ApiResponse;
import com.duyanhnguyen.petworld.backend.dto.response.FriendshipRequestResponse;
import com.duyanhnguyen.petworld.backend.service.FriendshipService;
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
@RequestMapping("/api/friendships")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FriendshipController {

    FriendshipService friendshipService;

    @GetMapping("/friendship-requests")
    public ApiResponse<Page<FriendshipRequestResponse>> getFriendshipRequests(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "100") Integer size
    ) {
        Long currentUserId = Long.parseLong(jwt.getSubject());
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        return ApiResponse.<Page<FriendshipRequestResponse>>builder()
                .message("Friendship requests retrieved successfully")
                .data(friendshipService.getFriendshipRequests(currentUserId, pageable))
                .build();
    }

    @PostMapping("/requests/{recipientId}")
    public ApiResponse<FriendshipRequestResponse> sendFriendRequest(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long recipientId
    ) {
        Long currentUserId = Long.parseLong(jwt.getSubject());
        return ApiResponse.<FriendshipRequestResponse>builder()
                .message("Friend request sent successfully")
                .data(friendshipService.sendFriendRequest(currentUserId, recipientId))
                .build();
    }

    @PutMapping("/{friendshipId}/accept")
    public ApiResponse<FriendshipRequestResponse> acceptFriendRequest(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long friendshipId
    ) {
        Long currentUserId = Long.parseLong(jwt.getSubject());
        return ApiResponse.<FriendshipRequestResponse>builder()
                .message("Friend request accepted successfully")
                .data(friendshipService.acceptFriendRequest(currentUserId, friendshipId))
                .build();
    }

    @DeleteMapping("/{friendshipId}/reject")
    public ApiResponse<Void> rejectFriendRequest(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long friendshipId
    ) {
        Long currentUserId = Long.parseLong(jwt.getSubject());
        friendshipService.rejectFriendRequest(currentUserId, friendshipId);
        return ApiResponse.<Void>builder()
                .message("Friend request rejected successfully")
                .build();
    }

    @DeleteMapping("/{friendshipId}/cancel")
    public ApiResponse<Void> cancelFriendRequest(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long friendshipId
    ) {
        Long currentUserId = Long.parseLong(jwt.getSubject());
        friendshipService.cancelFriendRequest(currentUserId, friendshipId);
        return ApiResponse.<Void>builder()
                .message("Friend request canceled successfully")
                .build();
    }

    @DeleteMapping("/{friendshipId}")
    public ApiResponse<Void> deleteFriendship(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long friendshipId
    ) {
        Long currentUserId = Long.parseLong(jwt.getSubject());
        friendshipService.deleteFriendship(currentUserId, friendshipId);
        return ApiResponse.<Void>builder()
                .message("Friendship deleted successfully")
                .build();
    }

}
