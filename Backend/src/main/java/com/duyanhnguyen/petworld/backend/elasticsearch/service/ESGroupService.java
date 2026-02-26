package com.duyanhnguyen.petworld.backend.elasticsearch.service;

import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ESGroupService {

    void index(Long groupId);

    List<Long> searchByKeyword(String keyword, Pageable pageable);

}
