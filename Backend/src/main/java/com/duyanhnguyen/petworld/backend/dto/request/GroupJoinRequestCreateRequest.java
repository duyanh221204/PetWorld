package com.duyanhnguyen.petworld.backend.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GroupJoinRequestCreateRequest {

    Long questionId;

    String answerText;

}
