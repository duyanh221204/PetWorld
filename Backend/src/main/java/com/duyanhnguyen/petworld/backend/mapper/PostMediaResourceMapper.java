package com.duyanhnguyen.petworld.backend.mapper;

import com.duyanhnguyen.petworld.backend.dto.request.PostMediaResourceRequest;
import com.duyanhnguyen.petworld.backend.dto.response.PostMediaResourceResponse;
import com.duyanhnguyen.petworld.backend.entity.PostMediaResourceEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PostMediaResourceMapper {

    PostMediaResourceEntity toEntity(PostMediaResourceRequest request);

    PostMediaResourceResponse toResponse(PostMediaResourceEntity entity);

    void update(PostMediaResourceRequest request, @MappingTarget PostMediaResourceEntity entity);

}
