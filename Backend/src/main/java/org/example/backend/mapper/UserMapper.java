package org.example.backend.mapper;

import org.example.backend.dto.request.UserRegistrationRequest;
import org.example.backend.dto.response.UserResponse;
import org.example.backend.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {

    @Mapping(target = "hashedPassword", ignore = true)
    UserEntity toEntity(UserRegistrationRequest userRegistrationRequest);

    UserResponse toResponse(UserEntity userEntity);

}
