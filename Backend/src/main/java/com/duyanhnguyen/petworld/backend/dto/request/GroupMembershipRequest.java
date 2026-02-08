package com.duyanhnguyen.petworld.backend.dto.request;

import com.duyanhnguyen.petworld.backend.enums.GroupRole;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GroupMembershipRequest {

    GroupRole role;

}
