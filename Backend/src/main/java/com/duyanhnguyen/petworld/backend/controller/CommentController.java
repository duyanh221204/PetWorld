package com.duyanhnguyen.petworld.backend.controller;

import com.duyanhnguyen.petworld.backend.dto.request.CommentCreateRequest;
import com.duyanhnguyen.petworld.backend.dto.request.CommentUpdateRequest;
import com.duyanhnguyen.petworld.backend.dto.response.ApiResponse;
import com.duyanhnguyen.petworld.backend.dto.response.CommentResponse;
import com.duyanhnguyen.petworld.backend.service.CommentService;
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

import java.util.List;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommentController {

    CommentService commentService;

    @GetMapping
    public ApiResponse<Page<CommentResponse>> getCommentsByPostId(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        Long currentUserId = Long.parseLong(jwt.getSubject());
        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        return ApiResponse.<Page<CommentResponse>>builder()
                .message("Comments retrieved successfully")
                .data(commentService.getCommentsByPostId(currentUserId, postId, pageable))
                .build();
    }

    @GetMapping("/{commentId}")
    public ApiResponse<CommentResponse> getCommentById(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long postId,
            @PathVariable Long commentId
    ) {
        Long currentUserId = Long.parseLong(jwt.getSubject());
        return ApiResponse.<CommentResponse>builder()
                .message("Comment retrieved successfully")
                .data(commentService.getCommentById(currentUserId, postId, commentId))
                .build();
    }

    @GetMapping("/{commentId}/replies")
    public ApiResponse<List<CommentResponse>> getRepliesByCommentId(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long postId,
            @PathVariable Long commentId
    ) {
        Long currentUserId = Long.parseLong(jwt.getSubject());
        return ApiResponse.<List<CommentResponse>>builder()
                .message("Replies retrieved successfully")
                .data(commentService.getRepliesByCommentId(currentUserId, postId, commentId))
                .build();
    }

    @PostMapping
    public ApiResponse<CommentResponse> createComment(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long postId,
            @RequestBody @Valid CommentCreateRequest request
    ) {
        Long currentUserId = Long.parseLong(jwt.getSubject());
        return ApiResponse.<CommentResponse>builder()
                .message("Comment created successfully")
                .data(commentService.createComment(currentUserId, postId, request))
                .build();
    }

    @PutMapping("/{commentId}")
    public ApiResponse<CommentResponse> updateComment(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestBody @Valid CommentUpdateRequest request
    ) {
        Long currentUserId = Long.parseLong(jwt.getSubject());
        return ApiResponse.<CommentResponse>builder()
                .message("Comment updated successfully")
                .data(commentService.updateComment(currentUserId, postId, commentId, request))
                .build();
    }

    @DeleteMapping("/{commentId}")
    public ApiResponse<Void> deleteComment(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long postId,
            @PathVariable Long commentId
    ) {
        Long currentUserId = Long.parseLong(jwt.getSubject());
        commentService.deleteComment(currentUserId, postId, commentId);
        return ApiResponse.<Void>builder()
                .message("Comment deleted successfully")
                .build();
    }

}
