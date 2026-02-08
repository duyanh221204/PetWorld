package com.duyanhnguyen.petworld.backend.elasticsearch.mapper;

import com.duyanhnguyen.petworld.backend.elasticsearch.document.ESUserDocument;
import com.duyanhnguyen.petworld.backend.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ESUserMapper {

    ESUserDocument toDocument(UserEntity entity);

}
