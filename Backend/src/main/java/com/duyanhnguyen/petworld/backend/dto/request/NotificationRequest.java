package com.duyanhnguyen.petworld.backend.dto.request;

import com.duyanhnguyen.petworld.backend.enums.NotificationType;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationRequest {

    @NotNull(message = "NOTIFICATION_TYPE_REQUIRED")
    NotificationType type;

    @NotNull(message = "RECIPIENT_ID_REQUIRED")
    Long recipientId;

    Long postId;

    Long commentId;

    Long friendshipId;

    Long groupId;

    Long groupJoinRequestId;

}
