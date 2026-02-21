package com.duyanhnguyen.petworld.backend.controller;

import com.duyanhnguyen.petworld.backend.dto.request.GroupJoinFormRequest;
import com.duyanhnguyen.petworld.backend.dto.response.ApiResponse;
import com.duyanhnguyen.petworld.backend.dto.response.GroupJoinFormResponse;
import com.duyanhnguyen.petworld.backend.service.GroupJoinFormService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups/{groupId}/join-forms")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GroupJoinFormController {

    GroupJoinFormService groupJoinFormService;

    @GetMapping
    public ApiResponse<List<GroupJoinFormResponse>> getGroupJoinForms(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long groupId
    ) {
        Long currentUserId = Long.parseLong(jwt.getSubject());
        return ApiResponse.<List<GroupJoinFormResponse>>builder()
                .message("Group join forms retrieved successfully")
                .data(groupJoinFormService.getGroupJoinForms(currentUserId, groupId))
                .build();
    }

    @GetMapping("/active")
    public ApiResponse<GroupJoinFormResponse> getActiveGroupJoinForm(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long groupId
    ) {
        Long currentUserId = Long.parseLong(jwt.getSubject());
        return ApiResponse.<GroupJoinFormResponse>builder()
                .message("Active group join form retrieved successfully")
                .data(groupJoinFormService.getActiveGroupJoinForm(currentUserId, groupId))
                .build();
    }

    @PostMapping
    public ApiResponse<GroupJoinFormResponse> createGroupJoinForm(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long groupId,
            @RequestBody @Valid GroupJoinFormRequest request
    ) {
        Long currentUserId = Long.parseLong(jwt.getSubject());
        return ApiResponse.<GroupJoinFormResponse>builder()
                .message("Group join form created successfully")
                .data(groupJoinFormService.createGroupJoinForm(currentUserId, groupId, request))
                .build();
    }

    @PutMapping("/{formId}")
    public ApiResponse<GroupJoinFormResponse> updateGroupJoinForm(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long groupId,
            @PathVariable Long formId,
            @RequestBody @Valid GroupJoinFormRequest request
    ) {
        Long currentUserId = Long.parseLong(jwt.getSubject());
        return ApiResponse.<GroupJoinFormResponse>builder()
                .message("Group join form updated successfully")
                .data(groupJoinFormService.updateGroupJoinForm(currentUserId, groupId, formId, request))
                .build();
    }

    @PutMapping("/{formId}/activate")
    public ApiResponse<GroupJoinFormResponse> activateGroupJoinForm(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long groupId,
            @PathVariable Long formId
    ) {
        Long currentUserId = Long.parseLong(jwt.getSubject());
        return ApiResponse.<GroupJoinFormResponse>builder()
                .message("Group join form activated successfully")
                .data(groupJoinFormService.activateGroupJoinForm(currentUserId, groupId, formId))
                .build();
    }

    @DeleteMapping("/{formId}")
    public ApiResponse<Void> deleteGroupJoinForm(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long groupId,
            @PathVariable Long formId
    ) {
        Long currentUserId = Long.parseLong(jwt.getSubject());
        groupJoinFormService.deleteGroupJoinForm(currentUserId, groupId, formId);
        return ApiResponse.<Void>builder()
                .message("Group join form deleted successfully")
                .build();
    }

}
