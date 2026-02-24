package com.duyanhnguyen.petworld.backend.dto.payload;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatReadPayload {

    Long chatId;

    Long readerId;

}
