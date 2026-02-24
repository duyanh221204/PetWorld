package com.duyanhnguyen.petworld.backend.service;

import com.duyanhnguyen.petworld.backend.dto.request.ChatMessageRequest;
import com.duyanhnguyen.petworld.backend.dto.response.ChatMessageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ChatMessageService {

    void sendMessage(Long currentUserId, ChatMessageRequest chatMessageRequest);

    Page<ChatMessageResponse> getChatMessages(Long currentUserId, Long chatId, Pageable pageable);

    void markAsRead(Long currentUserId, Long chatId);

}
