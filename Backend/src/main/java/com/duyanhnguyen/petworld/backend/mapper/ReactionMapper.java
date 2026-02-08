package com.duyanhnguyen.petworld.backend.mapper;

import com.duyanhnguyen.petworld.backend.dto.response.ReactionResponse;
import com.duyanhnguyen.petworld.backend.entity.ReactionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ReactionMapper {

    @Mapping(source = "sender.id", target = "userId")
    @Mapping(source = "sender.username", target = "username")
    @Mapping(source = "sender.avatar", target = "userAvatar")
    ReactionResponse toResponse(ReactionEntity entity);

}
