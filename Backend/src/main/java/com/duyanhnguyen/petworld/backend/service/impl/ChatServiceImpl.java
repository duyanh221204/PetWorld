package com.duyanhnguyen.petworld.backend.service.impl;

import com.duyanhnguyen.petworld.backend.dto.response.ChatResponse;
import com.duyanhnguyen.petworld.backend.entity.ChatEntity;
import com.duyanhnguyen.petworld.backend.entity.UserEntity;
import com.duyanhnguyen.petworld.backend.mapper.ChatMapper;
import com.duyanhnguyen.petworld.backend.repository.ChatRepository;
import com.duyanhnguyen.petworld.backend.service.ChatService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatServiceImpl implements ChatService {

    ChatRepository chatRepository;
    ChatMapper chatMapper;

    @Override
    public Page<ChatResponse> getAllChats(Long currentUserId, Pageable pageable) {
        Page<ChatEntity> chatsPage = chatRepository.findByUserId(currentUserId, pageable);
        if (chatsPage.isEmpty())
            return Page.empty(pageable);

        return chatsPage.map(
                chat -> {
                    UserEntity otherUser = chat.getUser1().getId().equals(currentUserId)
                            ? chat.getUser2()
                            : chat.getUser1();
                    Boolean hasUnread = chat.getUser1().getId().equals(currentUserId)
                            ? chat.getUser1HasUnread()
                            : chat.getUser2HasUnread();

                    ChatResponse chatResponse = chatMapper.toResponse(chat);
                    chatResponse.setName(otherUser.getUsername());
                    chatResponse.setAvatar(otherUser.getAvatar());
                    chatResponse.setHasUnread(hasUnread);
                    return chatResponse;
                }
        );
    }

    @Override
    public Long getUnreadCount(Long currentUserId) {
        return chatRepository.countUnreadChatsByUserId(currentUserId);
    }

}
