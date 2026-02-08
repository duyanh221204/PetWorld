package com.duyanhnguyen.petworld.backend.listener;

import com.duyanhnguyen.petworld.backend.entity.ChatEntity;
import com.duyanhnguyen.petworld.backend.enums.ErrorCode;
import com.duyanhnguyen.petworld.backend.event.GroupChatCreateEvent;
import com.duyanhnguyen.petworld.backend.exception.AppException;
import com.duyanhnguyen.petworld.backend.mapper.ChatMapper;
import com.duyanhnguyen.petworld.backend.repository.ChatRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GroupChatCreateEventListener {

    SimpMessagingTemplate simpMessagingTemplate;
    ChatRepository chatRepository;
    ChatMapper chatMapper;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(GroupChatCreateEvent groupChatCreateEvent) {
        ChatEntity chatEntity = chatRepository.findById(groupChatCreateEvent.getChatId())
                .orElseThrow(() -> new AppException(ErrorCode.CHAT_NOT_FOUND));
        groupChatCreateEvent.getParticipantId().forEach(
                participantId -> simpMessagingTemplate.convertAndSendToUser(
                        Long.toString(participantId),
                        "/queue/chats",
                        chatMapper.toResponse(chatEntity)
                )
        );
    }

}
