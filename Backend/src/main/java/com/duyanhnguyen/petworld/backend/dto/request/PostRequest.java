package com.duyanhnguyen.petworld.backend.dto.request;

import com.duyanhnguyen.petworld.backend.enums.PostVisibility;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PostRequest {

    String content;

    List<PostMediaResourceRequest> postMediaResources;

    @NotNull(message = "POST_VISIBILITY_REQUIRED")
    PostVisibility visibility;

    Long groupId;

}
