package com.duyanhnguyen.petworld.backend.service.impl;

import com.duyanhnguyen.petworld.backend.dto.request.NotificationRequest;
import com.duyanhnguyen.petworld.backend.dto.response.NotificationResponse;
import com.duyanhnguyen.petworld.backend.entity.NotificationEntity;
import com.duyanhnguyen.petworld.backend.enums.ErrorCode;
import com.duyanhnguyen.petworld.backend.event.NotificationCreateEvent;
import com.duyanhnguyen.petworld.backend.exception.AppException;
import com.duyanhnguyen.petworld.backend.mapper.NotificationMapper;
import com.duyanhnguyen.petworld.backend.repository.*;
import com.duyanhnguyen.petworld.backend.service.NotificationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationServiceImpl implements NotificationService {

    NotificationRepository notificationRepository;
    NotificationMapper notificationMapper;
    UserRepository userRepository;
    PostRepository postRepository;
    CommentRepository commentRepository;
    FriendshipRepository friendshipRepository;
    GroupRepository groupRepository;
    GroupJoinRequestRepository groupJoinRequestRepository;
    ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    @Override
    public void sendNotification(Long senderId, NotificationRequest notificationRequest) {
        NotificationEntity notificationEntity = notificationMapper.toEntity(notificationRequest);
        notificationEntity.setSender(
                userRepository.findById(senderId)
                        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND))
        );
        notificationEntity.setRecipient(
                userRepository.findById(notificationRequest.getRecipientId())
                        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND))
        );

        if (notificationRequest.getPostId() != null) {
            notificationEntity.setPost(
                    postRepository.findById(notificationRequest.getPostId())
                            .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND))
            );
        }
        if (notificationRequest.getCommentId() != null) {
            notificationEntity.setComment(
                    commentRepository.findById(notificationRequest.getCommentId())
                            .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND))
            );
        }
        if (notificationRequest.getFriendshipId() != null) {
            notificationEntity.setFriendship(
                    friendshipRepository.findById(notificationRequest.getFriendshipId())
                            .orElseThrow(() -> new AppException(ErrorCode.FRIENDSHIP_NOT_FOUND))
            );
        }
        if (notificationRequest.getGroupId() != null) {
            notificationEntity.setGroup(
                    groupRepository.findById(notificationRequest.getGroupId())
                            .orElseThrow(() -> new AppException(ErrorCode.GROUP_NOT_FOUND))
            );
        }
        if (notificationRequest.getGroupJoinRequestId() != null) {
            notificationEntity.setGroupJoinRequest(
                    groupJoinRequestRepository.findById(notificationRequest.getGroupJoinRequestId())
                            .orElseThrow(() -> new AppException(ErrorCode.GROUP_JOIN_REQUEST_NOT_FOUND))
            );
        }

        NotificationResponse notificationResponse = notificationMapper.toResponse(notificationRepository.save(notificationEntity));
        applicationEventPublisher.publishEvent(new NotificationCreateEvent(notificationResponse));
    }

    @Override
    public Page<NotificationResponse> getNotifications(Long currentUserId, Pageable pageable) {
        Page<NotificationEntity> notificationsPage = notificationRepository
                .findByRecipientIdOrderByCreatedAtDesc(currentUserId, pageable);
        return notificationsPage.map(notificationMapper::toResponse);
    }

    @Override
    public Long getUnreadCount(Long currentUserId) {
        return notificationRepository.countByRecipientIdAndIsRead(currentUserId, false);
    }

    @Transactional
    @Override
    public NotificationResponse markAsRead(Long currentUserId, Long notificationId) {
        NotificationEntity notificationEntity = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new AppException(ErrorCode.NOTIFICATION_NOT_FOUND));

        if (!notificationEntity.getRecipient().getId().equals(currentUserId))
            throw new AppException(ErrorCode.UNAUTHORIZED);

        notificationEntity.setIsRead(true);
        return notificationMapper.toResponse(notificationEntity);
    }

    @Transactional
    @Override
    public Long markAllAsRead(Long currentUserId) {
        return notificationRepository.markAllAsRead(currentUserId);
    }

}
