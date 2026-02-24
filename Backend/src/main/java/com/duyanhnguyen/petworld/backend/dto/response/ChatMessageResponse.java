package com.duyanhnguyen.petworld.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatMessageResponse {

    Long id;

    Long chatId;

    String content;

    Instant createdAt;

    Boolean isRead;

    Long senderId;

    String senderUsername;

    String senderAvatar;

}
