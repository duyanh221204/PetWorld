package com.duyanhnguyen.petworld.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GroupJoinFormQuestionOrderUpdateRequest {

    @NotNull(message = "GROUP_JOIN_FORM_QUESTION_ID_REQUIRED")
    Long id;

    @NotNull(message = "GROUP_JOIN_FORM_QUESTION_ORDER_REQUIRED")
    Integer questionOrder;

}
