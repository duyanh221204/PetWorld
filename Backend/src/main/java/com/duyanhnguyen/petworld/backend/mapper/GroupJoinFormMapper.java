package com.duyanhnguyen.petworld.backend.mapper;

import com.duyanhnguyen.petworld.backend.dto.request.GroupJoinFormRequest;
import com.duyanhnguyen.petworld.backend.dto.response.GroupJoinFormResponse;
import com.duyanhnguyen.petworld.backend.entity.GroupJoinFormEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface GroupJoinFormMapper {

    GroupJoinFormEntity toEntity(GroupJoinFormRequest request);

    @Mapping(source = "creator.id", target = "creatorId")
    @Mapping(source = "creator.username", target = "creatorUsername")
    GroupJoinFormResponse toResponse(GroupJoinFormEntity entity);

    List<GroupJoinFormResponse> toResponseList(List<GroupJoinFormEntity> groupJoinForms);

    void update(GroupJoinFormRequest request, @MappingTarget GroupJoinFormEntity entity);

}
