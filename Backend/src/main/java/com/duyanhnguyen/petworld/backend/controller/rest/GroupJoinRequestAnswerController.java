package com.duyanhnguyen.petworld.backend.controller.rest;

import com.duyanhnguyen.petworld.backend.dto.response.ApiResponse;
import com.duyanhnguyen.petworld.backend.dto.response.GroupJoinRequestAnswerResponse;
import com.duyanhnguyen.petworld.backend.service.GroupJoinRequestAnswerService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/groups/{groupId}/join-requests/{requestId}/answers")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GroupJoinRequestAnswerController {

    GroupJoinRequestAnswerService groupJoinRequestAnswerService;

    @GetMapping
    public ApiResponse<List<GroupJoinRequestAnswerResponse>> getAnswers(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long groupId,
            @PathVariable Long requestId
    ) {
        Long currentUserId = Long.parseLong(jwt.getSubject());
        return ApiResponse.<List<GroupJoinRequestAnswerResponse>>builder()
                .message("Group join request answers retrieved successfully")
                .data(groupJoinRequestAnswerService.getAnswers(currentUserId, groupId, requestId))
                .build();
    }

}
