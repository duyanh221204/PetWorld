package com.duyanhnguyen.petworld.backend.mapper;

import com.duyanhnguyen.petworld.backend.dto.request.NotificationRequest;
import com.duyanhnguyen.petworld.backend.dto.response.NotificationResponse;
import com.duyanhnguyen.petworld.backend.entity.NotificationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface NotificationMapper {

    NotificationEntity toEntity(NotificationRequest request);

    @Mapping(source = "sender.id", target = "senderId")
    @Mapping(source = "sender.username", target = "senderUsername")
    @Mapping(source = "sender.avatar", target = "senderAvatar")
    @Mapping(source = "recipient.id", target = "recipientId")
    @Mapping(source = "post.id", target = "postId")
    @Mapping(source = "comment.id", target = "commentId")
    @Mapping(source = "comment.rootComment.id", target = "rootCommentId")
    @Mapping(source = "friendship.id", target = "friendshipId")
    @Mapping(source = "group.id", target = "groupId")
    NotificationResponse toResponse(NotificationEntity entity);

}
