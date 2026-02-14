package com.duyanhnguyen.petworld.backend.controller;

import com.duyanhnguyen.petworld.backend.dto.request.GroupChatCreateRequest;
import com.duyanhnguyen.petworld.backend.dto.response.ApiResponse;
import com.duyanhnguyen.petworld.backend.dto.response.ChatResponse;
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

    @PostMapping("/groups")
    public ApiResponse<ChatResponse> createGroupChat(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody @Valid GroupChatCreateRequest groupChatCreateRequest
    ) {
        Long currentUserId = Long.parseLong(jwt.getSubject());
        return ApiResponse.<ChatResponse>builder()
                .message("Group chat created successfully")
                .data(chatService.createGroupChat(currentUserId, groupChatCreateRequest))
                .build();
    }

}
