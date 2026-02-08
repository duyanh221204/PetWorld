package com.duyanhnguyen.petworld.backend.listener;

import com.duyanhnguyen.petworld.backend.entity.NotificationEntity;
import com.duyanhnguyen.petworld.backend.enums.ErrorCode;
import com.duyanhnguyen.petworld.backend.event.NotificationCreateEvent;
import com.duyanhnguyen.petworld.backend.exception.AppException;
import com.duyanhnguyen.petworld.backend.mapper.NotificationMapper;
import com.duyanhnguyen.petworld.backend.repository.NotificationRepository;
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
    NotificationRepository notificationRepository;
    NotificationMapper notificationMapper;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(NotificationCreateEvent notificationCreateEvent) {
        NotificationEntity notificationEntity = notificationRepository.findById(notificationCreateEvent.getNotificationId())
                        .orElseThrow(() -> new AppException(ErrorCode.NOTIFICATION_NOT_FOUND));
        simpMessagingTemplate.convertAndSendToUser(
                Long.toString(notificationEntity.getRecipient().getId()),
                "/queue/notifications",
                notificationMapper.toResponse(notificationEntity)
        );
    }

}
