package com.duyanhnguyen.petworld.backend.elasticsearch.service.impl;

import com.duyanhnguyen.petworld.backend.elasticsearch.document.ESGroupDocument;
import com.duyanhnguyen.petworld.backend.elasticsearch.mapper.ESGroupMapper;
import com.duyanhnguyen.petworld.backend.elasticsearch.repository.ESGroupRepository;
import com.duyanhnguyen.petworld.backend.elasticsearch.service.ESGroupService;
import com.duyanhnguyen.petworld.backend.entity.GroupEntity;
import com.duyanhnguyen.petworld.backend.repository.GroupRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ESGroupServiceImpl implements ESGroupService {

    ESGroupRepository esGroupRepository;
    ESGroupMapper esGroupMapper;
    GroupRepository groupRepository;

    @Override
    public void index(Long groupId) {
        GroupEntity groupEntity = groupRepository.findById(groupId).orElse(null);
        if (groupEntity != null)
            esGroupRepository.save(esGroupMapper.toDocument(groupEntity));
        else
            esGroupRepository.deleteById(groupId);
    }

    @Override
    public List<Long> searchByKeyword(String keyword, Pageable pageable) {
        Sort sort = Sort.by("_score").descending().and(Sort.by("createdAt").descending())
                .and(Sort.by("id").descending());
        Pageable esPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        Page<ESGroupDocument> esGroupsPage = esGroupRepository.searchByName(keyword, esPageable);
        return esGroupsPage.map(ESGroupDocument::getId).getContent();
    }

}
