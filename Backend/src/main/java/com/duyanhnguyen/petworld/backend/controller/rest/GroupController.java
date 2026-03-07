package com.duyanhnguyen.petworld.backend.controller.rest;

import com.duyanhnguyen.petworld.backend.dto.request.GroupRequest;
import com.duyanhnguyen.petworld.backend.dto.response.ApiResponse;
import com.duyanhnguyen.petworld.backend.dto.response.GroupResponse;
import com.duyanhnguyen.petworld.backend.service.GroupService;
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
@RequestMapping("/api/groups")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GroupController {

    GroupService groupService;

    @GetMapping("/me/owned")
    public ApiResponse<Page<GroupResponse>> getOwnedGroups(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "100") Integer size
    ) {
        Long currentUserId = Long.parseLong(jwt.getSubject());
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        return ApiResponse.<Page<GroupResponse>>builder()
                .message("Owned groups retrieved successfully")
                .data(groupService.getOwnedGroups(currentUserId, pageable))
                .build();
    }

    @GetMapping("/me/joined")
    public ApiResponse<Page<GroupResponse>> getJoinedGroups(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "100") Integer size
    ) {
        Long currentUserId = Long.parseLong(jwt.getSubject());
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        return ApiResponse.<Page<GroupResponse>>builder()
                .message("Joined groups retrieved successfully")
                .data(groupService.getJoinedGroups(currentUserId, pageable))
                .build();
    }

    @GetMapping("/me/requests")
    public ApiResponse<Page<GroupResponse>> getJoinRequestedGroups(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "100") Integer size
    ) {
        Long currentUserId = Long.parseLong(jwt.getSubject());
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        return ApiResponse.<Page<GroupResponse>>builder()
                .message("Join requested groups retrieved successfully")
                .data(groupService.getJoinRequestedGroups(currentUserId, pageable))
                .build();
    }

    @GetMapping("/discover")
    public ApiResponse<Page<GroupResponse>> getGroupsNotJoinedOrRequested(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "100") Integer size
    ) {
        Long currentUserId = Long.parseLong(jwt.getSubject());
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        return ApiResponse.<Page<GroupResponse>>builder()
                .message("Groups not joined or requested retrieved successfully")
                .data(groupService.getGroupsNotJoinedOrRequested(currentUserId, pageable))
                .build();
    }

    @GetMapping("/search")
    public ApiResponse<Page<GroupResponse>> searchByName(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "100") Integer size
    ) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        return ApiResponse.<Page<GroupResponse>>builder()
                .message("Groups retrieved successfully")
                .data(groupService.searchByName(keyword, pageable))
                .build();
    }

    @GetMapping("/{groupId}")
    public ApiResponse<GroupResponse> getGroupById(@AuthenticationPrincipal Jwt jwt, @PathVariable Long groupId) {
        Long currentUserId = Long.parseLong(jwt.getSubject());
        return ApiResponse.<GroupResponse>builder()
                .message("Group retrieved successfully")
                .data(groupService.getGroupById(currentUserId, groupId))
                .build();
    }

    @PostMapping
    public ApiResponse<GroupResponse> createGroup(@AuthenticationPrincipal Jwt jwt, @RequestBody @Valid GroupRequest request) {
        Long currentUserId = Long.parseLong(jwt.getSubject());
        return ApiResponse.<GroupResponse>builder()
                .message("Group created successfully")
                .data(groupService.createGroup(currentUserId, request))
                .build();
    }

    @PutMapping("/{groupId}")
    public ApiResponse<GroupResponse> updateGroup(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long groupId,
            @RequestBody @Valid GroupRequest request
    ) {
        Long currentUserId = Long.parseLong(jwt.getSubject());
        return ApiResponse.<GroupResponse>builder()
                .message("Group updated successfully")
                .data(groupService.updateGroup(currentUserId, groupId, request))
                .build();
    }

    @DeleteMapping("/{groupId}")
    public ApiResponse<Void> deleteGroup(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long groupId
    ) {
        Long currentUserId = Long.parseLong(jwt.getSubject());
        groupService.deleteGroup(currentUserId, groupId);
        return ApiResponse.<Void>builder()
                .message("Group deleted successfully")
                .build();
    }

}
