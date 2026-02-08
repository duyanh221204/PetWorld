package com.duyanhnguyen.petworld.backend.mapper;

import com.duyanhnguyen.petworld.backend.dto.request.GroupJoinFormQuestionRequest;
import com.duyanhnguyen.petworld.backend.dto.response.GroupJoinFormQuestionResponse;
import com.duyanhnguyen.petworld.backend.entity.GroupJoinFormQuestionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface GroupJoinFormQuestionMapper {

    GroupJoinFormQuestionEntity toEntity(GroupJoinFormQuestionRequest request);

    GroupJoinFormQuestionResponse toResponse(GroupJoinFormQuestionEntity entity);

    List<GroupJoinFormQuestionResponse> toResponseList(List<GroupJoinFormQuestionEntity> groupJoinFormQuestions);

    void update(GroupJoinFormQuestionRequest request, @MappingTarget GroupJoinFormQuestionEntity entity);

}
