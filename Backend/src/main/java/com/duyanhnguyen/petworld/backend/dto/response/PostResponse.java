package com.duyanhnguyen.petworld.backend.dto.response;

import com.duyanhnguyen.petworld.backend.enums.PostVisibility;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PostResponse {

    Long id;

    String content;

    Instant createdAt;

    Instant updatedAt;

    List<PostMediaResourceResponse> postMediaResources;

    Long userId;

    String username;

    String userAvatar;

    Long reactionCount;

    Long commentCount;

    PostVisibility visibility;

    Long groupId;

    String groupName;

    Boolean isReactedByCurrentUser;

}
