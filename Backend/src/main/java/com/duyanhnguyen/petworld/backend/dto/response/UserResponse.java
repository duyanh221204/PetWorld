package com.duyanhnguyen.petworld.backend.dto.response;

import com.duyanhnguyen.petworld.backend.enums.Role;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {

    Long id;

    String username;

    String email;

    String avatar;

    String description;

    Long friendCount;

    Long postCount;

    Role role;

    Boolean isActive;

}
