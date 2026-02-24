package com.duyanhnguyen.petworld.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatMessageRequest {

    @NotNull(message = "CHAT_RECIPIENT_ID_REQUIRED")
    Long recipientId;

    @NotBlank(message = "CHAT_MESSAGE_CONTENT_REQUIRED")
    String content;

}
