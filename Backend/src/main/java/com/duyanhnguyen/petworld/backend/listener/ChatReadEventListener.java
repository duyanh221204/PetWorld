package com.duyanhnguyen.petworld.backend.listener;

import com.duyanhnguyen.petworld.backend.dto.payload.ChatReadPayload;
import com.duyanhnguyen.petworld.backend.event.ChatReadEvent;
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
public class ChatReadEventListener {

    SimpMessagingTemplate simpMessagingTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ChatReadEvent chatReadEvent) {
        simpMessagingTemplate.convertAndSendToUser(
                Long.toString(chatReadEvent.getToUserId()),
                "/queue/chats/read",
                new ChatReadPayload(chatReadEvent.getChatId(), chatReadEvent.getReaderId())
        );
    }

}
