package com.duyanhnguyen.petworld.backend.controller;

import com.duyanhnguyen.petworld.backend.dto.request.PostRequest;
import com.duyanhnguyen.petworld.backend.dto.response.ApiResponse;
import com.duyanhnguyen.petworld.backend.dto.response.PostResponse;
import com.duyanhnguyen.petworld.backend.service.PostService;
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
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PostController {

    PostService postService;

    @GetMapping
    public ApiResponse<Page<PostResponse>> getPostsForNewsFeed(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long currentUserId = Long.parseLong(jwt.getSubject());
        Pageable pageable = PageRequest.of(page, Math.min(size, 10));
        return ApiResponse.<Page<PostResponse>>builder()
                .message("Posts retrieved successfully")
                .data(postService.getPostsForNewsFeed(currentUserId, pageable))
                .build();
    }

    @GetMapping("/groups")
    public ApiResponse<Page<PostResponse>> getGroupPostsForNewsFeed(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long currentUserId = Long.parseLong(jwt.getSubject());
        Pageable pageable = PageRequest.of(page, Math.min(size, 10));
        return ApiResponse.<Page<PostResponse>>builder()
                .message("Groups' posts retrieved successfully")
                .data(postService.getGroupsPostsForNewsFeed(currentUserId, pageable))
                .build();
    }

    @GetMapping("/friends")
    public ApiResponse<Page<PostResponse>> getFriendsPostsForNewsFeed(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long currentUserId = Long.parseLong(jwt.getSubject());
        Pageable pageable = PageRequest.of(page, Math.min(size, 10));
        return ApiResponse.<Page<PostResponse>>builder()
                .message("Friends' posts retrieved successfully")
                .data(postService.getFriendsPostsForNewsFeed(currentUserId, pageable))
                .build();
    }

    @GetMapping("/groups/{groupId}")
    public ApiResponse<Page<PostResponse>> getPostsByGroupId(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long groupId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long currentUserId = Long.parseLong(jwt.getSubject());
        Pageable pageable = PageRequest.of(page, Math.min(size, 10));
        return ApiResponse.<Page<PostResponse>>builder()
                .message("Group's posts retrieved successfully")
                .data(postService.getPostsByGroupId(currentUserId, groupId, pageable))
                .build();
    }

    @GetMapping("/users/{userId}")
    public ApiResponse<Page<PostResponse>> getPostsByUserIdForProfile(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long currentUserId = Long.parseLong(jwt.getSubject());
        Pageable pageable = PageRequest.of(page, Math.min(size, 10));
        return ApiResponse.<Page<PostResponse>>builder()
                .message("User's posts retrieved successfully")
                .data(postService.getPostsByUserIdForProfile(currentUserId, userId, pageable))
                .build();
    }

    @GetMapping("/{postId}")
    public ApiResponse<PostResponse> getPostById(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long postId
    ) {
        Long currentUserId = Long.parseLong(jwt.getSubject());
        return ApiResponse.<PostResponse>builder()
                .message("Post retrieved successfully")
                .data(postService.getPostById(currentUserId, postId))
                .build();
    }

    @PostMapping
    public ApiResponse<PostResponse> createPost(@AuthenticationPrincipal Jwt jwt, @RequestBody @Valid PostRequest request) {
        Long currentUserId = Long.parseLong(jwt.getSubject());
        return ApiResponse.<PostResponse>builder()
                .message("Post created successfully")
                .data(postService.createPost(currentUserId, request))
                .build();
    }

    @PutMapping("/{postId}")
    public ApiResponse<PostResponse> updatePost(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long postId,
            @RequestBody @Valid PostRequest request
    ) {
        Long currentUserId = Long.parseLong(jwt.getSubject());
        return ApiResponse.<PostResponse>builder()
                .message("Post updated successfully")
                .data(postService.updatePost(currentUserId, postId, request))
                .build();
    }

    @DeleteMapping("/{postId}")
    public ApiResponse<Void> deletePost(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long postId
    ) {
        Long currentUserId = Long.parseLong(jwt.getSubject());
        postService.deletePost(currentUserId, postId);
        return ApiResponse.<Void>builder()
                .message("Post deleted successfully")
                .build();
    }

}
