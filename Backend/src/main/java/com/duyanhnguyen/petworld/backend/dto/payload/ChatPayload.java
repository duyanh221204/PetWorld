package com.duyanhnguyen.petworld.backend.dto.payload;

import com.duyanhnguyen.petworld.backend.dto.response.ChatMessageResponse;
import com.duyanhnguyen.petworld.backend.dto.response.ChatResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatPayload {

    ChatResponse info;

    ChatMessageResponse message;

}
