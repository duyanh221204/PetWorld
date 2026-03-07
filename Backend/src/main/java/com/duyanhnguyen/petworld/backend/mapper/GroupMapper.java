package com.duyanhnguyen.petworld.backend.mapper;

import com.duyanhnguyen.petworld.backend.dto.request.GroupRequest;
import com.duyanhnguyen.petworld.backend.dto.response.GroupResponse;
import com.duyanhnguyen.petworld.backend.entity.GroupEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface GroupMapper {

    GroupEntity toEntity(GroupRequest request);

    GroupResponse toResponse(GroupEntity entity);

    List<GroupResponse> toResponseList(List<GroupEntity> groups);

    void update(GroupRequest request, @MappingTarget GroupEntity entity);

}
