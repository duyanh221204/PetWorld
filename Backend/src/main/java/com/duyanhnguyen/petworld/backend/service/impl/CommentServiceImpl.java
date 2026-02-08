package com.duyanhnguyen.petworld.backend.service.impl;

import com.duyanhnguyen.petworld.backend.dto.request.CommentCreateRequest;
import com.duyanhnguyen.petworld.backend.dto.request.CommentUpdateRequest;
import com.duyanhnguyen.petworld.backend.dto.request.NotificationRequest;
import com.duyanhnguyen.petworld.backend.dto.response.CommentResponse;
import com.duyanhnguyen.petworld.backend.entity.CommentEntity;
import com.duyanhnguyen.petworld.backend.entity.PostEntity;
import com.duyanhnguyen.petworld.backend.entity.UserEntity;
import com.duyanhnguyen.petworld.backend.enums.ErrorCode;
import com.duyanhnguyen.petworld.backend.enums.NotificationType;
import com.duyanhnguyen.petworld.backend.enums.PostVisibility;
import com.duyanhnguyen.petworld.backend.exception.AppException;
import com.duyanhnguyen.petworld.backend.mapper.CommentMapper;
import com.duyanhnguyen.petworld.backend.repository.CommentRepository;
import com.duyanhnguyen.petworld.backend.repository.GroupMembershipRepository;
import com.duyanhnguyen.petworld.backend.repository.PostRepository;
import com.duyanhnguyen.petworld.backend.repository.UserRepository;
import com.duyanhnguyen.petworld.backend.service.CommentService;
import com.duyanhnguyen.petworld.backend.service.NotificationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommentServiceImpl implements CommentService {

    CommentRepository commentRepository;
    CommentMapper commentMapper;
    GroupMembershipRepository groupMembershipRepository;
    PostRepository postRepository;
    UserRepository userRepository;
    NotificationService notificationService;

    @Override
    public Page<CommentResponse> getCommentsByPostId(Long currentUserId, Long postId, Pageable pageable) {
        validateCommentViewPermission(currentUserId, postId);

        Page<CommentEntity> commentsPage = commentRepository
                .findByPostIdAndParentCommentIsNullOrderByCreatedAtDesc(postId, pageable);

        List<CommentResponse> commentResponses = commentMapper.toResponseList(commentsPage.getContent());
        Map<Long, Long> replyCounts = getReplyCounts(commentsPage.getContent());

        commentResponses.forEach(
                response -> response.setReplyCount(replyCounts.getOrDefault(response.getId(), 0L))
        );
        return new PageImpl<>(commentResponses, pageable, commentsPage.getTotalElements());
    }

    @Override
    public CommentResponse getCommentById(Long currentUserId, Long postId, Long commentId) {
        validateCommentViewPermission(currentUserId, postId);
        CommentEntity commentEntity = commentRepository.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));
        return commentMapper.toResponse(commentEntity);
    }

    @Override
    public List<CommentResponse> getRepliesByCommentId(Long currentUserId, Long postId, Long commentId) {
        validateCommentViewPermission(currentUserId, postId);

        if (!commentRepository.existsById(commentId))
            throw new AppException(ErrorCode.COMMENT_NOT_FOUND);

        List<CommentEntity> replies = commentRepository
                .findByParentCommentIdOrderByCreatedAtAsc(commentId);

        List<CommentResponse> responses = commentMapper.toResponseList(replies);
        responses.forEach(
                response -> response.setParentCommentId(commentId)
        );

        Map<Long, Long> replyCounts = getReplyCounts(replies);
        responses.forEach(
                response -> response.setReplyCount(replyCounts.getOrDefault(response.getId(), 0L))
        );
        return responses;
    }

    @Transactional
    @Override
    public CommentResponse createComment(Long currentUserId, Long postId, CommentCreateRequest commentCreateRequest) {
        UserEntity sender = userRepository.findById(currentUserId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        PostEntity postEntity = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

        CommentEntity parentComment = null;
        if (commentCreateRequest.getParentCommentId() != null) {
            parentComment = commentRepository.findById(commentCreateRequest.getParentCommentId())
                    .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));
            if (!parentComment.getPost().getId().equals(postId))
                throw new AppException(ErrorCode.INVALID_PARENT_COMMENT);
        }

        CommentEntity commentEntity = commentMapper.toEntity(commentCreateRequest);
        commentEntity.setSender(sender);
        commentEntity.setPost(postEntity);
        commentEntity.setParentComment(parentComment);

        CommentEntity toSave = commentRepository.save(commentEntity);

        if (!sender.getId().equals(postEntity.getCreator().getId()) &&
                (postEntity.getGroup() == null ||
                        groupMembershipRepository.existsByUserIdAndGroupId(postEntity.getCreator().getId(), postEntity.getGroup().getId())))
            notificationService.sendNotification(
                    sender.getId(),
                    NotificationRequest.builder()
                            .type(NotificationType.POST_COMMENTED)
                            .recipientId(postEntity.getCreator().getId())
                            .postId(postEntity.getId())
                            .commentId(toSave.getId())
                            .build()
            );

        if (parentComment != null && !sender.getId().equals(parentComment.getSender().getId()) &&
                (postEntity.getGroup() == null ||
                        groupMembershipRepository.existsByUserIdAndGroupId(parentComment.getSender().getId(), postEntity.getGroup().getId())))
            notificationService.sendNotification(
                    sender.getId(),
                    NotificationRequest.builder()
                            .type(NotificationType.COMMENT_REPLIED)
                            .recipientId(parentComment.getSender().getId())
                            .postId(postEntity.getId())
                            .commentId(toSave.getId())
                            .build()
            );

        return commentMapper.toResponse(commentEntity);
    }

    @Transactional
    @Override
    public CommentResponse updateComment(Long currentUserId, Long postId, Long commentId, CommentUpdateRequest commentUpdateRequest) {
        CommentEntity commentEntity = validateCommentSender(currentUserId, postId, commentId);
        commentEntity.setContent(commentUpdateRequest.getContent());
        commentEntity.setUpdatedAt(Instant.now());
        return commentMapper.toResponse(commentRepository.save(commentEntity));
    }

    @Transactional
    @Override
    public void deleteComment(Long currentUserId, Long postId, Long commentId) {
        CommentEntity commentEntity = validateCommentSender(currentUserId, postId, commentId);
        commentRepository.delete(commentEntity);
    }

    private Map<Long, Long> getReplyCounts(List<CommentEntity> comments) {
        if (comments == null || comments.isEmpty())
            return Collections.emptyMap();

        List<Long> commentIds = comments.stream()
                .map(CommentEntity::getId)
                .collect(Collectors.toList());

        List<Object[]> counts = commentRepository.countRepliesByParentCommentIds(commentIds);
        return counts.stream().collect(Collectors.toMap(reply -> (Long) reply[0], reply -> (Long) reply[1]));
    }

    private void validateCommentViewPermission(Long currentUserId, Long postId) {
        PostEntity postEntity = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));
        if (postEntity.getVisibility() == PostVisibility.PRIVATE)
            throw new AppException(ErrorCode.UNAUTHORIZED);
        if (postEntity.getVisibility() == PostVisibility.GROUP_ONLY) {
            if (!groupMembershipRepository.existsByUserIdAndGroupId(currentUserId, postEntity.getGroup().getId()))
                throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }

    private CommentEntity validateCommentSender(Long currentUserId, Long postId, Long commentId) {
        UserEntity userEntity = userRepository.findById(currentUserId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        validateCommentViewPermission(currentUserId, postId);
        CommentEntity commentEntity = commentRepository.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));

        if (!commentEntity.getSender().getId().equals(userEntity.getId()))
            throw new AppException(ErrorCode.UNAUTHORIZED);
        return commentEntity;
    }

}
