package com.duyanhnguyen.petworld.backend.dto.response;

import com.duyanhnguyen.petworld.backend.enums.NotificationType;
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
public class NotificationResponse {

    Long id;

    NotificationType type;

    Instant createdAt;

    Boolean isRead;

    Long senderId;

    String senderUsername;

    String senderAvatar;

    Long recipientId;

    Long postId;

    Long commentId;

    Long friendshipId;

    Long groupId;

}
