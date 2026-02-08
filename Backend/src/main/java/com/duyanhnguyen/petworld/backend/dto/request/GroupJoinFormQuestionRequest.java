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
public class GroupJoinFormQuestionRequest {

    @NotBlank(message = "GROUP_JOIN_FORM_QUESTION_TEXT_REQUIRED")
    String questionText;

    @NotNull(message = "GROUP_JOIN_FORM_QUESTION_IS_REQUIRED_REQUIRED")
    Boolean isRequired;

    Integer questionOrder;

}
