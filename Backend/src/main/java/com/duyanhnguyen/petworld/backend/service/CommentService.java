package com.duyanhnguyen.petworld.backend.service;

import com.duyanhnguyen.petworld.backend.dto.request.CommentCreateRequest;
import com.duyanhnguyen.petworld.backend.dto.request.CommentUpdateRequest;
import com.duyanhnguyen.petworld.backend.dto.response.CommentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CommentService {

    Page<CommentResponse> getCommentsByPostId(Long currentUserId, Long postId, Pageable pageable);

    CommentResponse getCommentById(Long currentUserId, Long postId, Long commentId);

    List<CommentResponse> getRepliesByCommentId(Long currentUserId, Long postId, Long commentId);

    CommentResponse createComment(Long currentUserId, Long postId, CommentCreateRequest commentCreateRequest);

    CommentResponse updateComment(Long currentUserId, Long postId, Long commentId, CommentUpdateRequest commentUpdateRequest);

    void deleteComment(Long currentUserId, Long postId, Long commentId);

}
