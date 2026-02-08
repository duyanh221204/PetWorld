package com.duyanhnguyen.petworld.backend.dto.request;

import jakarta.validation.constraints.Email;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserActivationRequest {

    @Email(message = "INVALID_EMAIL")
    String email;

    String verificationCode;

}
