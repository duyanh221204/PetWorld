package com.duyanhnguyen.petworld.backend.elasticsearch.mapper;

import com.duyanhnguyen.petworld.backend.elasticsearch.document.ESPostDocument;
import com.duyanhnguyen.petworld.backend.entity.PostEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ESPostMapper {

    @Mapping(source = "group.id", target = "groupId")
    ESPostDocument toDocument(PostEntity entity);

}
