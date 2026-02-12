package com.duyanhnguyen.petworld.backend.service.impl;

import com.duyanhnguyen.petworld.backend.dto.request.NotificationRequest;
import com.duyanhnguyen.petworld.backend.dto.response.FriendshipRequestResponse;
import com.duyanhnguyen.petworld.backend.dto.response.FriendshipStatusResponse;
import com.duyanhnguyen.petworld.backend.dto.response.UserResponse;
import com.duyanhnguyen.petworld.backend.entity.FriendshipEntity;
import com.duyanhnguyen.petworld.backend.entity.UserEntity;
import com.duyanhnguyen.petworld.backend.enums.ErrorCode;
import com.duyanhnguyen.petworld.backend.enums.FriendshipStatus;
import com.duyanhnguyen.petworld.backend.enums.NotificationType;
import com.duyanhnguyen.petworld.backend.exception.AppException;
import com.duyanhnguyen.petworld.backend.mapper.FriendshipMapper;
import com.duyanhnguyen.petworld.backend.mapper.UserMapper;
import com.duyanhnguyen.petworld.backend.repository.FriendshipRepository;
import com.duyanhnguyen.petworld.backend.repository.UserRepository;
import com.duyanhnguyen.petworld.backend.service.FriendshipService;
import com.duyanhnguyen.petworld.backend.service.NotificationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FriendshipServiceImpl implements FriendshipService {

    FriendshipRepository friendshipRepository;
    UserRepository userRepository;
    FriendshipMapper friendshipMapper;
    NotificationService notificationService;
    UserMapper userMapper;

    @Override
    public Page<FriendshipRequestResponse> getFriendshipRequests(Long currentUserId, Pageable pageable) {
        Page<FriendshipEntity> friendshipRequestsPage = friendshipRepository
                .findByRecipientIdAndAcceptedAtIsNullOrderBySentAtDesc(currentUserId, pageable);
        return friendshipRequestsPage.map(friendshipMapper::toResponse);
    }

    @Override
    public Page<UserResponse> getFriendsList(Long currentUserId, Pageable pageable) {
        Page<UserEntity> friendshipsPage = friendshipRepository.findFriendsOfUser(currentUserId, pageable);
        return friendshipsPage.map(userMapper::toResponse);
    }

    @Transactional
    @Override
    public FriendshipRequestResponse sendFriendRequest(Long currentUserId, Long recipientId) {
        if (currentUserId.equals(recipientId))
            throw new AppException(ErrorCode.INVALID_FRIENDSHIP_REQUEST);

        UserEntity sender = userRepository.findById(currentUserId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        UserEntity recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (friendshipRepository.existsByAcceptedAtIsNullAndSenderIdAndRecipientId(currentUserId, recipientId) ||
                friendshipRepository.existsByAcceptedAtIsNullAndSenderIdAndRecipientId(recipientId, currentUserId))
            throw new AppException(ErrorCode.FRIENDSHIP_REQUEST_ALREADY_EXISTS);
        if (friendshipRepository.existsByAcceptedAtIsNotNullAndSenderIdAndRecipientId(currentUserId, recipientId) ||
                friendshipRepository.existsByAcceptedAtIsNotNullAndSenderIdAndRecipientId(recipientId, currentUserId))
            throw new AppException(ErrorCode.ALREADY_FRIENDS);

        FriendshipEntity friendshipEntity = FriendshipEntity.builder()
                .sender(sender)
                .recipient(recipient)
                .build();

        FriendshipEntity toSave = friendshipRepository.save(friendshipEntity);
        notificationService.sendNotification(
                sender.getId(),
                NotificationRequest.builder()
                        .type(NotificationType.FRIEND_REQUEST_RECEIVED)
                        .recipientId(recipient.getId())
                        .friendshipId(toSave.getId())
                        .build()
        );
        return friendshipMapper.toResponse(toSave);
    }

    @Transactional
    @Override
    public FriendshipRequestResponse acceptFriendRequest(Long currentUserId, Long friendshipId) {
        FriendshipEntity friendshipEntity = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new AppException(ErrorCode.FRIENDSHIP_NOT_FOUND));

        if (!friendshipEntity.getRecipient().getId().equals(currentUserId))
            throw new AppException(ErrorCode.UNAUTHORIZED);
        if (friendshipEntity.getAcceptedAt() != null)
            throw new AppException(ErrorCode.ALREADY_FRIENDS);

        friendshipEntity.setAcceptedAt(Instant.now());

        notificationService.sendNotification(
                currentUserId,
                NotificationRequest.builder()
                        .type(NotificationType.FRIEND_REQUEST_ACCEPTED)
                        .recipientId(friendshipEntity.getSender().getId())
                        .friendshipId(friendshipEntity.getId())
                        .build()
        );
        return friendshipMapper.toResponse(friendshipEntity);
    }

    @Transactional
    @Override
    public void rejectFriendRequest(Long currentUserId, Long friendshipId) {
        FriendshipEntity friendshipEntity = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new AppException(ErrorCode.FRIENDSHIP_NOT_FOUND));

        if (!friendshipEntity.getRecipient().getId().equals(currentUserId))
            throw new AppException(ErrorCode.UNAUTHORIZED);
        if (friendshipEntity.getAcceptedAt() != null)
            throw new AppException(ErrorCode.ALREADY_FRIENDS);

        friendshipRepository.delete(friendshipEntity);
    }

    @Transactional
    @Override
    public void cancelFriendRequest(Long currentUserId, Long friendshipId) {
        FriendshipEntity friendshipEntity = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new AppException(ErrorCode.FRIENDSHIP_NOT_FOUND));

        if (!friendshipEntity.getSender().getId().equals(currentUserId))
            throw new AppException(ErrorCode.UNAUTHORIZED);
        if (friendshipEntity.getAcceptedAt() != null)
            throw new AppException(ErrorCode.ALREADY_FRIENDS);

        friendshipRepository.delete(friendshipEntity);
    }

    @Transactional
    @Override
    public void deleteFriendship(Long currentUserId, Long friendshipId) {
        FriendshipEntity friendshipEntity = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new AppException(ErrorCode.FRIENDSHIP_NOT_FOUND));

        if (!friendshipEntity.getSender().getId().equals(currentUserId) &&
                !friendshipEntity.getRecipient().getId().equals(currentUserId))
            throw new AppException(ErrorCode.UNAUTHORIZED);
        if (friendshipEntity.getAcceptedAt() == null)
            throw new AppException(ErrorCode.NOT_FRIENDS);

        friendshipRepository.delete(friendshipEntity);
    }

    @Override
    public FriendshipStatusResponse getFriendshipStatus(Long currentUserId, Long otherUserId) {
        if (currentUserId.equals(otherUserId))
            throw new AppException(ErrorCode.INVALID_FRIENDSHIP_REQUEST);
        if (!userRepository.existsById(otherUserId))
            throw new AppException(ErrorCode.USER_NOT_FOUND);

        FriendshipStatusResponse friendshipStatusResponse = new FriendshipStatusResponse();
        FriendshipEntity friendshipEntity = friendshipRepository.findBetweenTwoUsers(currentUserId, otherUserId)
                .orElse(null);

        if (friendshipEntity == null)
            friendshipStatusResponse.setStatus(FriendshipStatus.NONE);
        else {
            friendshipStatusResponse.setId(friendshipEntity.getId());
            if (friendshipEntity.getAcceptedAt() != null)
                friendshipStatusResponse.setStatus(FriendshipStatus.FRIENDS);
            else if (friendshipEntity.getSender().getId().equals(currentUserId))
                friendshipStatusResponse.setStatus(FriendshipStatus.PENDING_SENT);
            else
                friendshipStatusResponse.setStatus(FriendshipStatus.PENDING_RECEIVED);
        }

        return friendshipStatusResponse;
    }

}
