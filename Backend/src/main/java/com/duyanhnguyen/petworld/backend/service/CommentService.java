package com.duyanhnguyen.petworld.backend.service;

import com.duyanhnguyen.petworld.backend.dto.request.CommentCreateRequest;
import com.duyanhnguyen.petworld.backend.dto.request.CommentUpdateRequest;
import com.duyanhnguyen.petworld.backend.dto.response.CommentResponse;
import com.duyanhnguyen.petworld.backend.dto.response.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CommentService {

    Page<CommentResponse> getCommentsByPostId(Long currentUserId, Long postId, Pageable pageable);

    PageResponse getCommentPage(Long currentUserId, Long postId, Long commentId, Integer size);

    List<CommentResponse> getRepliesByRootCommentId(Long currentUserId, Long postId, Long rootCommentId);

    CommentResponse createComment(Long currentUserId, Long postId, CommentCreateRequest commentCreateRequest);

    CommentResponse updateComment(Long currentUserId, Long postId, Long commentId, CommentUpdateRequest commentUpdateRequest);

    void deleteComment(Long currentUserId, Long postId, Long commentId);

}
