package com.duyanhnguyen.petworld.backend.service.impl;

import com.duyanhnguyen.petworld.backend.dto.request.GroupChatCreateRequest;
import com.duyanhnguyen.petworld.backend.dto.response.ChatResponse;
import com.duyanhnguyen.petworld.backend.entity.ChatEntity;
import com.duyanhnguyen.petworld.backend.entity.ChatParticipantEntity;
import com.duyanhnguyen.petworld.backend.enums.ChatParticipantRole;
import com.duyanhnguyen.petworld.backend.enums.ChatType;
import com.duyanhnguyen.petworld.backend.event.GroupChatCreateEvent;
import com.duyanhnguyen.petworld.backend.mapper.ChatMapper;
import com.duyanhnguyen.petworld.backend.repository.ChatRepository;
import com.duyanhnguyen.petworld.backend.repository.UserRepository;
import com.duyanhnguyen.petworld.backend.service.ChatService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatServiceImpl implements ChatService {

    ChatRepository chatRepository;
    ChatMapper chatMapper;
    UserRepository userRepository;
    ApplicationEventPublisher applicationEventPublisher;

    @Transactional(readOnly = true)
    @Override
    public Page<ChatResponse> getAllChats(Long currentUserId, Pageable pageable) {
        Page<ChatEntity> chatsPage = chatRepository.findByCurrentUserId(currentUserId, pageable);
        if (chatsPage.isEmpty())
            return Page.empty(pageable);

        List<ChatEntity> chatsPageContent = chatsPage.getContent();
        List<Long> chatIds = chatsPageContent.stream().map(ChatEntity::getId).collect(Collectors.toList());

        List<ChatEntity> privateChats = chatRepository.findByIdInAndType(chatIds, ChatType.PRIVATE);
        Map<Long, ChatEntity> privateChatsMap = privateChats.stream()
                .collect(Collectors.toMap(ChatEntity::getId, Function.identity()));

        List<ChatResponse> chatResponses = chatsPageContent.stream()
                .map(
                        chatEntity -> {
                            ChatResponse chatResponse = chatMapper.toResponse(chatEntity);
                            if (chatEntity.getType() == ChatType.PRIVATE) {
                                ChatEntity privateChat = privateChatsMap.get(chatEntity.getId());
                                for (ChatParticipantEntity chatParticipantEntity : privateChat.getChatParticipants()) {
                                    if (!chatParticipantEntity.getUser().getId().equals(currentUserId)) {
                                        chatResponse.setName(chatParticipantEntity.getUser().getUsername());
                                        chatResponse.setAvatar(chatParticipantEntity.getUser().getAvatar());
                                        break;
                                    }
                                }
                            }
                            return chatResponse;
                        }
                )
                .collect(Collectors.toList());

        return new PageImpl<>(chatResponses, pageable, chatsPage.getTotalElements());
    }

    @Transactional
    @Override
    public ChatResponse createGroupChat(Long currentUserId, GroupChatCreateRequest groupChatCreateRequest) {
        ChatEntity chatEntity = ChatEntity.builder()
                .type(ChatType.GROUP)
                .name(groupChatCreateRequest.getName())
                .avatar(groupChatCreateRequest.getAvatar())
                .lastMessagedAt(Instant.now())
                .build();
        chatEntity.getChatParticipants().add(
                ChatParticipantEntity.builder()
                        .user(userRepository.getReferenceById(currentUserId))
                        .chat(chatEntity)
                        .role(ChatParticipantRole.ADMIN)
                        .build()
        );

        groupChatCreateRequest.getParticipantIds().forEach(
                participantId -> chatEntity.getChatParticipants().add(
                        ChatParticipantEntity.builder()
                                .user(userRepository.getReferenceById(participantId))
                                .chat(chatEntity)
                                .role(ChatParticipantRole.MEMBER)
                                .build()
                )
        );

        ChatResponse chatResponse = chatMapper.toResponse(chatRepository.save(chatEntity));
        groupChatCreateRequest.getParticipantIds().add(currentUserId);
        applicationEventPublisher.publishEvent(
                new GroupChatCreateEvent(chatResponse.getId(), groupChatCreateRequest.getParticipantIds())
        );
        return chatResponse;
    }

}
