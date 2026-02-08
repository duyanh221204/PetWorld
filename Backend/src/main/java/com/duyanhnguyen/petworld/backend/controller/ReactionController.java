package com.duyanhnguyen.petworld.backend.controller;

import com.duyanhnguyen.petworld.backend.dto.response.ApiResponse;
import com.duyanhnguyen.petworld.backend.dto.response.ReactionResponse;
import com.duyanhnguyen.petworld.backend.service.ReactionService;
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
@RequestMapping("/api/posts/{postId}/reactions")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReactionController {

    ReactionService reactionService;

    @GetMapping
    public ApiResponse<Page<ReactionResponse>> getReactionsByPostId(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size
    ) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        return ApiResponse.<Page<ReactionResponse>>builder()
                .message("Reactions retrieved successfully")
                .data(reactionService.getReactionsByPostId(postId, pageable))
                .build();
    }

    @PostMapping
    public ApiResponse<Void> createReaction(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long postId
    ) {
        Long currentUserId = Long.parseLong(jwt.getSubject());
        reactionService.createReaction(currentUserId, postId);
        return ApiResponse.<Void>builder()
                .message("Reacted to post successfully")
                .build();
    }

    @DeleteMapping
    public ApiResponse<Void> deleteReaction(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long postId
    ) {
        Long currentUserId = Long.parseLong(jwt.getSubject());
        reactionService.deleteReaction(currentUserId, postId);
        return ApiResponse.<Void>builder()
                .message("Unreacted to post successfully")
                .build();
    }

}
