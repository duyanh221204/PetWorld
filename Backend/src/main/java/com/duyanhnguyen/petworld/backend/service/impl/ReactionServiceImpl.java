package com.duyanhnguyen.petworld.backend.service.impl;

import com.duyanhnguyen.petworld.backend.dto.request.NotificationRequest;
import com.duyanhnguyen.petworld.backend.dto.response.ReactionResponse;
import com.duyanhnguyen.petworld.backend.entity.PostEntity;
import com.duyanhnguyen.petworld.backend.entity.ReactionEntity;
import com.duyanhnguyen.petworld.backend.entity.UserEntity;
import com.duyanhnguyen.petworld.backend.enums.ErrorCode;
import com.duyanhnguyen.petworld.backend.enums.NotificationType;
import com.duyanhnguyen.petworld.backend.exception.AppException;
import com.duyanhnguyen.petworld.backend.mapper.ReactionMapper;
import com.duyanhnguyen.petworld.backend.repository.PostRepository;
import com.duyanhnguyen.petworld.backend.repository.ReactionRepository;
import com.duyanhnguyen.petworld.backend.repository.UserRepository;
import com.duyanhnguyen.petworld.backend.service.NotificationService;
import com.duyanhnguyen.petworld.backend.service.ReactionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReactionServiceImpl implements ReactionService {

    ReactionRepository reactionRepository;
    ReactionMapper reactionMapper;
    PostRepository postRepository;
    UserRepository userRepository;
    NotificationService notificationService;

    @Override
    public Page<ReactionResponse> getReactionsByPostId(Long postId, Pageable pageable) {
        if (!postRepository.existsById(postId))
            throw new AppException(ErrorCode.POST_NOT_FOUND);
        Page<ReactionEntity> reactionsPage = reactionRepository.findByPostIdOrderByCreatedAtDesc(postId, pageable);
        return reactionsPage.map(reactionMapper::toResponse);
    }

    @Transactional
    @Override
    public void createReaction(Long currentUserId, Long postId) {
        UserEntity sender = userRepository.findById(currentUserId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        PostEntity postEntity = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

        if (reactionRepository.existsBySenderIdAndPostId(currentUserId, postId))
            throw new AppException(ErrorCode.REACTION_ALREADY_EXISTS);

        ReactionEntity reactionEntity = ReactionEntity.builder()
                .sender(sender)
                .post(postEntity)
                .build();
        reactionRepository.save(reactionEntity);

        if (!sender.getId().equals(postEntity.getCreator().getId()))
            notificationService.sendNotification(
                    sender.getId(),
                    NotificationRequest.builder()
                            .type(NotificationType.POST_REACTED)
                            .recipientId(postEntity.getCreator().getId())
                            .postId(postEntity.getId())
                            .build()
            );
    }

    @Transactional
    @Override
    public void deleteReaction(Long currentUserId, Long postId) {
        ReactionEntity reactionEntity = reactionRepository.findBySenderIdAndPostId(currentUserId, postId)
                .orElseThrow(() -> new AppException(ErrorCode.REACTION_NOT_FOUND));
        reactionRepository.delete(reactionEntity);
    }

}
