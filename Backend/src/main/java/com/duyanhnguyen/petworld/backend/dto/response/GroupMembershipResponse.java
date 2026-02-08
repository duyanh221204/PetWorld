package com.duyanhnguyen.petworld.backend.dto.response;

import com.duyanhnguyen.petworld.backend.enums.GroupRole;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GroupMembershipResponse {

    Long id;

    Long groupId;

    Long userId;

    String username;

    GroupRole role;

}
