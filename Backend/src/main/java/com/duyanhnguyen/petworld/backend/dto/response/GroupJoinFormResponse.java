package com.duyanhnguyen.petworld.backend.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GroupJoinFormResponse {

    Long id;

    String title;

    Boolean isActive;

    Instant createdAt;

    Long creatorId;

    String creatorUsername;

}
