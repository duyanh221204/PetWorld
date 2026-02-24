package com.duyanhnguyen.petworld.backend.controller.rest;

import com.duyanhnguyen.petworld.backend.dto.response.ApiResponse;
import com.duyanhnguyen.petworld.backend.dto.response.ChatResponse;
import com.duyanhnguyen.petworld.backend.service.ChatMessageService;
import com.duyanhnguyen.petworld.backend.service.ChatService;
import jakarta.validation.Valid;
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
@RequestMapping("/api/chats")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatController {

    ChatService chatService;
    ChatMessageService chatMessageService;

    @GetMapping
    public ApiResponse<Page<ChatResponse>> getAllChats(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "50") Integer size
    ) {
        Long currentUserId = Long.parseLong(jwt.getSubject());
        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        return ApiResponse.<Page<ChatResponse>>builder()
                .message("Chats retrieved successfully")
                .data(chatService.getAllChats(currentUserId, pageable))
                .build();
    }

    @GetMapping("/unread-count")
    public ApiResponse<Long> getUnreadCount(@AuthenticationPrincipal Jwt jwt) {
        Long currentUserId = Long.parseLong(jwt.getSubject());
        return ApiResponse.<Long>builder()
                .message("Unread chat count retrieved successfully")
                .data(chatService.getUnreadCount(currentUserId))
                .build();
    }

    @PutMapping("/{chatId}/read")
    public ApiResponse<Void> markAsRead(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long chatId
    ) {
        Long currentUserId = Long.parseLong(jwt.getSubject());
        chatMessageService.markAsRead(currentUserId, chatId);
        return ApiResponse.<Void>builder()
                .message("Chat marked as read successfully")
                .build();
    }

}
