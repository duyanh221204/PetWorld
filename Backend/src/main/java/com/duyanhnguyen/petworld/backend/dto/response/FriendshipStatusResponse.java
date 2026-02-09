package com.duyanhnguyen.petworld.backend.dto.response;

import com.duyanhnguyen.petworld.backend.enums.FriendshipStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FriendshipStatusResponse {

    Long id;

    FriendshipStatus status;

}
