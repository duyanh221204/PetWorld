package com.duyanhnguyen.petworld.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentCreateRequest {

    @NotBlank(message = "COMMENT_CONTENT_REQUIRED")
    String content;

    Long parentCommentId;

}
