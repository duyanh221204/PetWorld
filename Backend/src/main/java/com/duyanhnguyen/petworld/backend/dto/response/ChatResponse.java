package com.duyanhnguyen.petworld.backend.dto.response;

import com.duyanhnguyen.petworld.backend.enums.ChatType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatResponse {

    Long id;

    ChatType type;

    String name;

    String avatar;

    Instant lastMessagedAt;

}
