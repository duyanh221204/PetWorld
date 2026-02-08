package com.duyanhnguyen.petworld.backend.elasticsearch.service;

import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ESPostService {

    void index(Long postId);

    List<Long> getPostIdsForNewsFeedByKeyword(String keyword, Pageable pageable);

    List<Long> getPostIdsForGroupByKeyword(String keyword, Long groupId, Pageable pageable);

}
