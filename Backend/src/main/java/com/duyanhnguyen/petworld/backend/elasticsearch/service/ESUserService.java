package com.duyanhnguyen.petworld.backend.elasticsearch.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ESUserService {

    void index(Long userId);

    Page<Long> searchByKeyword(String keyword, Pageable pageable);

}
