package com.duyanhnguyen.petworld.backend.mapper;

import com.duyanhnguyen.petworld.backend.dto.response.GroupJoinRequestResponse;
import com.duyanhnguyen.petworld.backend.entity.GroupJoinRequestEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface GroupJoinRequestMapper {

    @Mapping(source = "sender.id", target = "senderId")
    @Mapping(source = "sender.username", target = "senderUsername")
    @Mapping(source = "sender.avatar", target = "senderAvatar")
    GroupJoinRequestResponse toResponse(GroupJoinRequestEntity entity);

}
