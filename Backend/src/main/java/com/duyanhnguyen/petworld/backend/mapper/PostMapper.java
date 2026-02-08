package com.duyanhnguyen.petworld.backend.mapper;

import com.duyanhnguyen.petworld.backend.dto.request.PostRequest;
import com.duyanhnguyen.petworld.backend.dto.response.PostResponse;
import com.duyanhnguyen.petworld.backend.entity.PostEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = { PostMediaResourceMapper.class }
)
public interface PostMapper {

    PostEntity toEntity(PostRequest request);

    @Mapping(source = "creator.id", target = "userId")
    @Mapping(source = "creator.username", target = "username")
    @Mapping(source = "creator.avatar", target = "userAvatar")
    @Mapping(source = "group.id", target = "groupId")
    @Mapping(source = "group.name", target = "groupName")
    PostResponse toResponse(PostEntity entity);

    @Mapping(target = "postMediaResources", ignore = true)
    void update(PostRequest request, @MappingTarget PostEntity entity);

}
