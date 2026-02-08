package com.duyanhnguyen.petworld.backend.mapper;

import com.duyanhnguyen.petworld.backend.dto.request.CommentCreateRequest;
import com.duyanhnguyen.petworld.backend.dto.response.CommentResponse;
import com.duyanhnguyen.petworld.backend.entity.CommentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CommentMapper {

    CommentEntity toEntity(CommentCreateRequest request);

    @Mapping(source = "sender.id", target = "senderId")
    @Mapping(source = "sender.username", target = "senderUsername")
    @Mapping(source = "sender.avatar", target = "senderAvatar")
    @Mapping(source = "post.id", target = "postId")
    CommentResponse toResponse(CommentEntity entity);

    List<CommentResponse> toResponseList(List<CommentEntity> comments);

}
