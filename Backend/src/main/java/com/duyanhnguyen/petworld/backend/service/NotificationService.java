package com.duyanhnguyen.petworld.backend.service;

import com.duyanhnguyen.petworld.backend.dto.request.NotificationRequest;
import com.duyanhnguyen.petworld.backend.dto.response.NotificationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {

    void sendNotification(Long senderId, NotificationRequest notificationRequest);

    Page<NotificationResponse> getNotifications(Long currentUserId, Pageable pageable);

    Long getUnreadCount(Long currentUserId);

    NotificationResponse markAsRead(Long currentUserId, Long notificationId);

    Long markAllAsRead(Long currentUserId);

}
