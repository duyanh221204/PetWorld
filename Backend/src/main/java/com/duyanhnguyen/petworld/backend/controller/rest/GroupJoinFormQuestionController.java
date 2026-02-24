package com.duyanhnguyen.petworld.backend.controller.rest;

import com.duyanhnguyen.petworld.backend.dto.request.GroupJoinFormQuestionOrderUpdateRequest;
import com.duyanhnguyen.petworld.backend.dto.request.GroupJoinFormQuestionRequest;
import com.duyanhnguyen.petworld.backend.dto.response.ApiResponse;
import com.duyanhnguyen.petworld.backend.dto.response.GroupJoinFormQuestionResponse;
import com.duyanhnguyen.petworld.backend.service.GroupJoinFormQuestionService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups/{groupId}/join-forms/{formId}/questions")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GroupJoinFormQuestionController {

    GroupJoinFormQuestionService groupJoinFormQuestionService;

    @GetMapping
    public ApiResponse<List<GroupJoinFormQuestionResponse>> getGroupJoinFormQuestions(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long groupId,
            @PathVariable Long formId
    ) {
        Long currentUserId = Long.parseLong(jwt.getSubject());
        return ApiResponse.<List<GroupJoinFormQuestionResponse>>builder()
                .message("Group join form questions retrieved successfully")
                .data(groupJoinFormQuestionService.getGroupJoinFormQuestions(currentUserId, groupId, formId))
                .build();
    }

    @PostMapping
    public ApiResponse<GroupJoinFormQuestionResponse> createGroupJoinFormQuestion(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long groupId,
            @PathVariable Long formId,
            @RequestBody @Valid GroupJoinFormQuestionRequest request
    ) {
        Long currentUserId = Long.parseLong(jwt.getSubject());
        return ApiResponse.<GroupJoinFormQuestionResponse>builder()
                .message("Group join form question created successfully")
                .data(groupJoinFormQuestionService.createGroupJoinFormQuestion(currentUserId, groupId, formId, request))
                .build();
    }

    @PutMapping("/{questionId}")
    public ApiResponse<GroupJoinFormQuestionResponse> updateGroupJoinFormQuestion(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long groupId,
            @PathVariable Long formId,
            @PathVariable Long questionId,
            @RequestBody @Valid GroupJoinFormQuestionRequest request
    ) {
        Long currentUserId = Long.parseLong(jwt.getSubject());
        return ApiResponse.<GroupJoinFormQuestionResponse>builder()
                .message("Group join form question updated successfully")
                .data(groupJoinFormQuestionService.updateGroupJoinFormQuestion(currentUserId, groupId, formId, questionId, request))
                .build();
    }

    @DeleteMapping("/{questionId}")
    public ApiResponse<Void> deleteGroupJoinFormQuestion(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long groupId,
            @PathVariable Long formId,
            @PathVariable Long questionId
    ) {
        Long currentUserId = Long.parseLong(jwt.getSubject());
        groupJoinFormQuestionService.deleteGroupJoinFormQuestion(currentUserId, groupId, formId, questionId);
        return ApiResponse.<Void>builder()
                .message("Group join form question deleted successfully")
                .build();
    }

    @PutMapping("/reorder")
    public ApiResponse<Void> updateGroupJoinFormQuestionOrders(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long groupId,
            @PathVariable Long formId,
            @RequestBody @Valid List<GroupJoinFormQuestionOrderUpdateRequest> requests
    ) {
        Long currentUserId = Long.parseLong(jwt.getSubject());
        groupJoinFormQuestionService.updateGroupJoinFormQuestionOrders(currentUserId, groupId, formId, requests);
        return ApiResponse.<Void>builder()
                .message("Group join form question orders updated successfully")
                .build();
    }

}
