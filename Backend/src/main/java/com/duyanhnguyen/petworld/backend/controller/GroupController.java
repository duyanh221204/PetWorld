package com.duyanhnguyen.petworld.backend.controller;

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

    @GetMapping
    public ApiResponse<Page<GroupResponse>> getGroups(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "true") Boolean joined,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "100") Integer size
    ) {
        Long currentUserId = Long.parseLong(jwt.getSubject());
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        return ApiResponse.<Page<GroupResponse>>builder()
                .message("Groups retrieved successfully")
                .data(groupService.getGroups(currentUserId, joined, pageable))
                .build();
    }

    @GetMapping("/{groupId}")
    public ApiResponse<GroupResponse> getGroupById(@PathVariable Long groupId) {
        return ApiResponse.<GroupResponse>builder()
                .message("Group retrieved successfully")
                .data(groupService.getGroupById(groupId))
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
