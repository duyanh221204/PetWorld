package com.duyanhnguyen.petworld.backend.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GroupJoinRequestResponse {

    Long id;

    Long senderId;

    String senderUsername;

    String senderAvatar;

    Instant submittedAt;

}
