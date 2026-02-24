package com.duyanhnguyen.petworld.backend.event;

import com.duyanhnguyen.petworld.backend.dto.payload.ChatPayload;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatMessageCreateEvent {

    Long senderId;

    Long recipientId;

    ChatPayload senderPayload;

    ChatPayload recipientPayload;

}
