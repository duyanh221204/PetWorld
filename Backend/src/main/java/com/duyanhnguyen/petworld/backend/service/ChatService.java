package com.duyanhnguyen.petworld.backend.service;

import com.duyanhnguyen.petworld.backend.dto.request.GroupChatCreateRequest;
import com.duyanhnguyen.petworld.backend.dto.response.ChatResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ChatService {

    Page<ChatResponse> getAllChats(Long currentUserId, Pageable pageable);

    ChatResponse createGroupChat(Long currentUserId, GroupChatCreateRequest groupChatCreateRequest);

}
