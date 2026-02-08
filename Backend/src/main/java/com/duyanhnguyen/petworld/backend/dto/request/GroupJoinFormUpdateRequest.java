package com.duyanhnguyen.petworld.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GroupJoinFormUpdateRequest {

    @NotBlank(message = "GROUP_JOIN_FORM_TITLE_REQUIRED")
    String title;

}
