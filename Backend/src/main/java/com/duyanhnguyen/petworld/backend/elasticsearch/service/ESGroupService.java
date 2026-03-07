package com.duyanhnguyen.petworld.backend.elasticsearch.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ESGroupService {

    void index(Long groupId);

    Page<Long> searchByKeyword(String keyword, Pageable pageable);

}
