package com.duyanhnguyen.petworld.backend.controller.websocket;

import com.duyanhnguyen.petworld.backend.dto.request.ChatMessageRequest;
import com.duyanhnguyen.petworld.backend.service.ChatMessageService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatWebSocketController {

    ChatMessageService chatMessageService;

    @MessageMapping("/messages/send")
    public void sendMessage(Principal principal, @Payload @Valid ChatMessageRequest request) {
        chatMessageService.sendMessage(Long.parseLong(principal.getName()), request);
    }

}
