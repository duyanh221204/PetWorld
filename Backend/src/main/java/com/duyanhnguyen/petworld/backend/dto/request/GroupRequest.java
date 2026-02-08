package com.duyanhnguyen.petworld.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GroupRequest {

    @Size(min = 5, max = 100, message = "INVALID_GROUP_NAME")
    String name;

    String description;

    String coverImageUrl;

}
