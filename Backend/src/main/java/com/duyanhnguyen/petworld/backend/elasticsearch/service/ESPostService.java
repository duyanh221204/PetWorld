package com.duyanhnguyen.petworld.backend.elasticsearch.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ESPostService {

    void index(Long postId);

    Page<Long> searchByKeyword(String keyword, Pageable pageable);

    Page<Long> searchByKeywordInGroup(String keyword, Long groupId, Pageable pageable);

}
