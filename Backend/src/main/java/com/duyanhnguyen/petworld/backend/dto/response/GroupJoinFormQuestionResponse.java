package com.duyanhnguyen.petworld.backend.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GroupJoinFormQuestionResponse {

    Long id;

    String questionText;

    Boolean isRequired;

    Integer questionOrder;

}
