package com.duyanhnguyen.petworld.backend.service.impl;

import com.duyanhnguyen.petworld.backend.dto.payload.ChatPayload;
import com.duyanhnguyen.petworld.backend.dto.request.ChatMessageRequest;
import com.duyanhnguyen.petworld.backend.dto.response.ChatMessageResponse;
import com.duyanhnguyen.petworld.backend.dto.response.ChatResponse;
import com.duyanhnguyen.petworld.backend.entity.ChatEntity;
import com.duyanhnguyen.petworld.backend.entity.ChatMessageEntity;
import com.duyanhnguyen.petworld.backend.entity.UserEntity;
import com.duyanhnguyen.petworld.backend.enums.ErrorCode;
import com.duyanhnguyen.petworld.backend.event.ChatMessageCreateEvent;
import com.duyanhnguyen.petworld.backend.event.ChatReadEvent;
import com.duyanhnguyen.petworld.backend.exception.AppException;
import com.duyanhnguyen.petworld.backend.mapper.ChatMessageMapper;
import com.duyanhnguyen.petworld.backend.repository.ChatMessageRepository;
import com.duyanhnguyen.petworld.backend.repository.ChatRepository;
import com.duyanhnguyen.petworld.backend.repository.UserRepository;
import com.duyanhnguyen.petworld.backend.service.ChatMessageService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatMessageServiceImpl implements ChatMessageService {

    ChatMessageRepository chatMessageRepository;
    ChatMessageMapper chatMessageMapper;
    ChatRepository chatRepository;
    UserRepository userRepository;
    ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    @Override
    public void sendMessage(Long currentUserId, ChatMessageRequest chatMessageRequest) {
        if (currentUserId.equals(chatMessageRequest.getRecipientId()))
            throw new AppException(ErrorCode.INVALID_CHAT_MESSAGE);

        Long user1Id = Math.min(currentUserId, chatMessageRequest.getRecipientId());
        Long user2Id = Math.max(currentUserId, chatMessageRequest.getRecipientId());

        ChatEntity chatEntity = chatRepository.findByUser1IdAndUser2Id(user1Id, user2Id)
                .orElseGet(() -> ChatEntity.builder()
                        .user1(userRepository.getReferenceById(user1Id))
                        .user2(userRepository.getReferenceById(user2Id))
                        .build());

        chatEntity.setLastMessagedAt(Instant.now());
        chatEntity.setLastMessagePreview(chatMessageRequest.getContent());
        chatEntity.setLastSenderId(currentUserId);

        if (currentUserId.equals(user1Id))
            chatEntity.setUser2HasUnread(true);
        else
            chatEntity.setUser1HasUnread(true);

        chatRepository.save(chatEntity);

        UserEntity sender = userRepository.getReferenceById(currentUserId);
        UserEntity recipient = userRepository.getReferenceById(chatMessageRequest.getRecipientId());
        ChatMessageResponse chatMessageResponse = chatMessageMapper.toResponse(
                chatMessageRepository.save(
                        ChatMessageEntity.builder()
                                .content(chatMessageRequest.getContent())
                                .sender(sender)
                                .chat(chatEntity)
                                .build()
                )
        );

        ChatPayload senderPayload = ChatPayload.builder()
                .info(
                        ChatResponse.builder()
                                .id(chatEntity.getId())
                                .name(recipient.getUsername())
                                .avatar(recipient.getAvatar())
                                .lastMessagedAt(chatEntity.getLastMessagedAt())
                                .lastMessagePreview(chatEntity.getLastMessagePreview())
                                .lastSenderId(chatEntity.getLastSenderId())
                                .build()
                )
                .message(chatMessageResponse)
                .build();
        ChatPayload recipientPayload = ChatPayload.builder()
                .info(
                        ChatResponse.builder()
                                .id(chatEntity.getId())
                                .name(sender.getUsername())
                                .avatar(sender.getAvatar())
                                .lastMessagedAt(chatEntity.getLastMessagedAt())
                                .lastMessagePreview(chatEntity.getLastMessagePreview())
                                .lastSenderId(chatEntity.getLastSenderId())
                                .build()
                )
                .message(chatMessageResponse)
                .build();
        applicationEventPublisher.publishEvent(new ChatMessageCreateEvent(
                currentUserId, chatMessageRequest.getRecipientId(), senderPayload, recipientPayload
        ));
    }

    @Override
    public Page<ChatMessageResponse> getChatMessages(Long currentUserId, Long chatId, Pageable pageable) {
        ChatEntity chatEntity = chatRepository.findById(chatId)
                .orElseThrow(() -> new AppException(ErrorCode.CHAT_NOT_FOUND));
        if (!chatEntity.getUser1().getId().equals(currentUserId) && !chatEntity.getUser2().getId().equals(currentUserId))
            throw new AppException(ErrorCode.CHAT_NOT_FOUND);

        Page<ChatMessageEntity> chatMessagesPage = chatMessageRepository.findByChatIdOrderByCreatedAtDesc(chatId, pageable);
        return chatMessagesPage.map(chatMessageMapper::toResponse);
    }

    @Transactional
    @Override
    public void markAsRead(Long currentUserId, Long chatId) {
        ChatEntity chatEntity = chatRepository.findById(chatId)
                .orElseThrow(() -> new AppException(ErrorCode.CHAT_NOT_FOUND));
        if (!chatEntity.getUser1().getId().equals(currentUserId) && !chatEntity.getUser2().getId().equals(currentUserId))
            throw new AppException(ErrorCode.CHAT_NOT_FOUND);

        Long otherUserId;
        if (currentUserId.equals(chatEntity.getUser1().getId())) {
            chatEntity.setUser1HasUnread(false);
            otherUserId = chatEntity.getUser2().getId();
        }
        else {
            chatEntity.setUser2HasUnread(false);
            otherUserId = chatEntity.getUser1().getId();
        }
        chatMessageRepository.markAsRead(chatId, currentUserId);
        applicationEventPublisher.publishEvent(new ChatReadEvent(otherUserId, chatId, currentUserId));
    }

}
