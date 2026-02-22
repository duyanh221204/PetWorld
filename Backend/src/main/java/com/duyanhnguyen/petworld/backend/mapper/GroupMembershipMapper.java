package com.duyanhnguyen.petworld.backend.mapper;

import com.duyanhnguyen.petworld.backend.dto.request.GroupMembershipRequest;
import com.duyanhnguyen.petworld.backend.dto.response.GroupMembershipResponse;
import com.duyanhnguyen.petworld.backend.entity.GroupMembershipEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface GroupMembershipMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "user.avatar", target = "userAvatar")
    @Mapping(source = "group.id", target = "groupId")
    GroupMembershipResponse toResponse(GroupMembershipEntity entity);

    void update(GroupMembershipRequest request, @MappingTarget GroupMembershipEntity entity);

}
