package com.duyanhnguyen.petworld.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GroupChatCreateRequest {

    @NotBlank(message = "CHAT_NAME_REQUIRED")
    String name;

    String avatar;

    List<Long> participantIds;

}
