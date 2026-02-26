package com.duyanhnguyen.petworld.backend.elasticsearch.mapper;

import com.duyanhnguyen.petworld.backend.elasticsearch.document.ESGroupDocument;
import com.duyanhnguyen.petworld.backend.entity.GroupEntity;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ESGroupMapper {

    ESGroupDocument toDocument(GroupEntity entity);

}
