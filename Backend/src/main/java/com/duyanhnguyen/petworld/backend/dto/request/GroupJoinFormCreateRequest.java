package com.duyanhnguyen.petworld.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GroupJoinFormCreateRequest {

    @NotBlank(message = "GROUP_JOIN_FORM_TITLE_REQUIRED")
    String title;

    @NotNull(message = "GROUP_JOIN_FORM_IS_ACTIVE_REQUIRED")
    Boolean isActive;

}
