package com.duyanhnguyen.petworld.backend.controller;

import com.duyanhnguyen.petworld.backend.dto.request.GroupJoinRequestCreateRequest;
import com.duyanhnguyen.petworld.backend.dto.response.ApiResponse;
import com.duyanhnguyen.petworld.backend.dto.response.GroupJoinRequestResponse;
import com.duyanhnguyen.petworld.backend.service.GroupJoinRequestService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups/{groupId}/join-requests")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GroupJoinRequestController {

    GroupJoinRequestService groupJoinRequestService;

    @PostMapping
    public ApiResponse<Void> createGroupJoinRequest(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long groupId,
            @RequestBody(required = false) List<GroupJoinRequestCreateRequest> requests
    ) {
        Long currentUserId = Long.parseLong(jwt.getSubject());
        groupJoinRequestService.createGroupJoinRequest(currentUserId, groupId, requests);
        return ApiResponse.<Void>builder()
                .message("Group join request created successfully")
                .build();
    }

    @GetMapping
    public ApiResponse<Page<GroupJoinRequestResponse>> getGroupJoinRequests(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long groupId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "100") Integer size
    ) {
        Long currentUserId = Long.parseLong(jwt.getSubject());
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        return ApiResponse.<Page<GroupJoinRequestResponse>>builder()
                .message("Group join requests retrieved successfully")
                .data(groupJoinRequestService.getGroupJoinRequests(currentUserId, groupId, pageable))
                .build();
    }

    @PostMapping("/{requestId}/approve")
    public ApiResponse<Void> approveGroupJoinRequest(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long groupId,
            @PathVariable Long requestId
    ) {
        Long currentUserId = Long.parseLong(jwt.getSubject());
        groupJoinRequestService.approveGroupJoinRequest(currentUserId, groupId, requestId);
        return ApiResponse.<Void>builder()
                .message("Group join request approved successfully")
                .build();
    }

    @DeleteMapping("/{requestId}/reject")
    public ApiResponse<Void> rejectGroupJoinRequest(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long groupId,
            @PathVariable Long requestId
    ) {
        Long currentUserId = Long.parseLong(jwt.getSubject());
        groupJoinRequestService.rejectGroupJoinRequest(currentUserId, groupId, requestId);
        return ApiResponse.<Void>builder()
                .message("Group join request rejected successfully")
                .build();
    }

    @DeleteMapping("/{requestId}/cancel")
    public ApiResponse<Void> cancelGroupJoinRequest(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long groupId,
            @PathVariable Long requestId
    ) {
        Long currentUserId = Long.parseLong(jwt.getSubject());
        groupJoinRequestService.cancelGroupJoinRequest(currentUserId, groupId, requestId);
        return ApiResponse.<Void>builder()
                .message("Group join request canceled successfully")
                .build();
    }

}
