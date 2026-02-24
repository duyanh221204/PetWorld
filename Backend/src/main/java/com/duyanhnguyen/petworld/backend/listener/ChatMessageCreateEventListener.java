package com.duyanhnguyen.petworld.backend.listener;

import com.duyanhnguyen.petworld.backend.event.ChatMessageCreateEvent;
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
public class ChatMessageCreateEventListener {

    SimpMessagingTemplate simpMessagingTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ChatMessageCreateEvent chatMessageCreateEvent) {
        simpMessagingTemplate.convertAndSendToUser(
                Long.toString(chatMessageCreateEvent.getRecipientId()),
                "/queue/messages",
                chatMessageCreateEvent.getRecipientPayload()
        );
        simpMessagingTemplate.convertAndSendToUser(
                Long.toString(chatMessageCreateEvent.getSenderId()),
                "/queue/messages",
                chatMessageCreateEvent.getSenderPayload()
        );
    }

}
