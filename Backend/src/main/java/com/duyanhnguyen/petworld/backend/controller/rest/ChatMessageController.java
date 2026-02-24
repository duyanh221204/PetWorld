package com.duyanhnguyen.petworld.backend.controller.rest;

import com.duyanhnguyen.petworld.backend.dto.response.ApiResponse;
import com.duyanhnguyen.petworld.backend.dto.response.ChatMessageResponse;
import com.duyanhnguyen.petworld.backend.service.ChatMessageService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatMessageController {

    ChatMessageService chatMessageService;

    @GetMapping
    public ApiResponse<Page<ChatMessageResponse>> getChatMessages(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam Long chatId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "50") Integer size
    ) {
        Long currentUserId = Long.parseLong(jwt.getSubject());
        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        return ApiResponse.<Page<ChatMessageResponse>>builder()
                .message("Chat messages retrieved successfully")
                .data(chatMessageService.getChatMessages(currentUserId, chatId, pageable))
                .build();
    }

}
