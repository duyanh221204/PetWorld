package com.duyanhnguyen.petworld.backend.controller;

import com.duyanhnguyen.petworld.backend.dto.response.ApiResponse;
import com.duyanhnguyen.petworld.backend.dto.response.NotificationResponse;
import com.duyanhnguyen.petworld.backend.service.NotificationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationController {

    NotificationService notificationService;

    @GetMapping
    public ApiResponse<Page<NotificationResponse>> getNotifications(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        Long currentUserId = Long.parseLong(jwt.getSubject());
        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        return ApiResponse.<Page<NotificationResponse>>builder()
                .message("Notifications retrieved successfully")
                .data(notificationService.getNotifications(currentUserId, pageable))
                .build();
    }

    @GetMapping("/unread-count")
    public ApiResponse<Long> getUnreadCount(@AuthenticationPrincipal Jwt jwt) {
        Long currentUserId = Long.parseLong(jwt.getSubject());
        return ApiResponse.<Long>builder()
                .message("Unread notifications count retrieved successfully")
                .data(notificationService.getUnreadCount(currentUserId))
                .build();
    }

    @PutMapping("/{notificationId}/mark-as-read")
    public ApiResponse<NotificationResponse> markAsRead(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long notificationId
    ) {
        Long currentUserId = Long.parseLong(jwt.getSubject());
        return ApiResponse.<NotificationResponse>builder()
                .message("Notification marked as read")
                .data(notificationService.markAsRead(currentUserId, notificationId))
                .build();
    }

    @PutMapping("/mark-all-as-read")
    public ApiResponse<Long> markAllAsRead(@AuthenticationPrincipal Jwt jwt) {
        Long currentUserId = Long.parseLong(jwt.getSubject());
        return ApiResponse.<Long>builder()
                .message("All notifications marked as read")
                .data(notificationService.markAllAsRead(currentUserId))
                .build();
    }

}
