package com.duyanhnguyen.petworld.backend.service;

import com.duyanhnguyen.petworld.backend.dto.request.PostRequest;
import com.duyanhnguyen.petworld.backend.dto.response.PostResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostService {

    PostResponse createPost(Long currentUserId, PostRequest postRequest);

    Page<PostResponse> getPostsForNewsFeed(Long currentUserId, Pageable pageable);

    Page<PostResponse> getGroupsPostsForNewsFeed(Long currentUserId, Pageable pageable);

    Page<PostResponse> getFriendsPostsForNewsFeed(Long currentUserId, Pageable pageable);

    Page<PostResponse> getPostsByGroupId(Long currentUserId, Long groupId, Pageable pageable);

    Page<PostResponse> getPostsByUserIdForProfile(Long currentUserId, Long userId, Pageable pageable);

    PostResponse getPostById(Long currentUserId, Long postId);

    PostResponse updatePost(Long currentUserId, Long postId, PostRequest postRequest);

    void deletePost(Long currentUserId, Long postId);

}
