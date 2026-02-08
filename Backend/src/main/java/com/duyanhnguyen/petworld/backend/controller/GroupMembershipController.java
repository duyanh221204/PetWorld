package com.duyanhnguyen.petworld.backend.controller;

import com.duyanhnguyen.petworld.backend.dto.request.GroupMembershipRequest;
import com.duyanhnguyen.petworld.backend.dto.response.ApiResponse;
import com.duyanhnguyen.petworld.backend.dto.response.GroupMembershipResponse;
import com.duyanhnguyen.petworld.backend.service.GroupMembershipService;
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
@RequestMapping("/api/groups/{groupId}/memberships")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GroupMembershipController {

    GroupMembershipService groupMembershipService;

    @GetMapping
    public ApiResponse<Page<GroupMembershipResponse>> getGroupMembers(
            @PathVariable Long groupId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size
    ) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        return ApiResponse.<Page<GroupMembershipResponse>>builder()
                .message("Group members retrieved successfully")
                .data(groupMembershipService.getGroupMemberships(groupId, pageable))
                .build();
    }

    @PutMapping("/{userId}/role")
    public ApiResponse<GroupMembershipResponse> updateGroupMembership(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long groupId,
            @PathVariable Long userId,
            @RequestBody @Valid GroupMembershipRequest request
    ) {
        Long currentUserId = Long.parseLong(jwt.getSubject());
        return ApiResponse.<GroupMembershipResponse>builder()
                .message("Group membership updated successfully")
                .data(groupMembershipService.updateGroupMembership(currentUserId, groupId, userId, request))
                .build();
    }

    @DeleteMapping("/{userId}")
    public ApiResponse<Void> deleteGroupMembership(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long groupId,
            @PathVariable Long userId
    ) {
        Long currentUserId = Long.parseLong(jwt.getSubject());
        groupMembershipService.deleteGroupMembership(currentUserId, groupId, userId);
        return ApiResponse.<Void>builder()
                .message("Group membership deleted successfully")
                .build();
    }

    @DeleteMapping("/leave")
    public ApiResponse<Void> leaveGroup(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long groupId
    ) {
        Long currentUserId = Long.parseLong(jwt.getSubject());
        groupMembershipService.leaveGroup(currentUserId, groupId);
        return ApiResponse.<Void>builder()
                .message("Left group successfully")
                .build();
    }

    @PostMapping("/transfer-ownership/{userId}")
    public ApiResponse<Void> transferGroupOwnershipAndLeave(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long groupId,
            @PathVariable Long userId
    ) {
        Long currentUserId = Long.parseLong(jwt.getSubject());
        groupMembershipService.transferGroupOwnershipAndLeave(currentUserId, groupId, userId);
        return ApiResponse.<Void>builder()
                .message("Group ownership transferred and left group successfully")
                .build();
    }

}
