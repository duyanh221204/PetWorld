package com.duyanhnguyen.petworld.backend.elasticsearch.service;

import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ESPostService {

    void index(Long postId);

    List<Long> searchByKeyword(String keyword, Pageable pageable);

    List<Long> searchByKeywordInGroup(String keyword, Long groupId, Pageable pageable);

}
