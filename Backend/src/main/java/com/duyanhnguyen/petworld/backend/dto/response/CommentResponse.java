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
public class CommentResponse {

    Long id;

    String content;

    Instant createdAt;

    Instant updatedAt;

    Long senderId;

    String senderUsername;

    String senderAvatar;

    Long parentCommentId;

    Long parentCommentSenderId;

    String parentCommentSenderUsername;

    String parentCommentSenderAvatar;

    Long rootCommentId;

    Long replyCount;

}
