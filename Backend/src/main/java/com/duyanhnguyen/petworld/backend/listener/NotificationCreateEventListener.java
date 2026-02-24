package com.duyanhnguyen.petworld.backend.listener;

import com.duyanhnguyen.petworld.backend.event.NotificationCreateEvent;
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
public class NotificationCreateEventListener {

    SimpMessagingTemplate simpMessagingTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(NotificationCreateEvent notificationCreateEvent) {
        simpMessagingTemplate.convertAndSendToUser(
                Long.toString(notificationCreateEvent.getNotificationResponse().getRecipientId()),
                "/queue/notifications",
                notificationCreateEvent.getNotificationResponse()
        );
    }

}
