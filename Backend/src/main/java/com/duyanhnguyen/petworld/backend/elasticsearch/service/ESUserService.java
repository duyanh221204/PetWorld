package com.duyanhnguyen.petworld.backend.elasticsearch.service;

import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ESUserService {

    void index(Long userId);

    List<Long> searchByKeyword(String keyword, Pageable pageable);

}
