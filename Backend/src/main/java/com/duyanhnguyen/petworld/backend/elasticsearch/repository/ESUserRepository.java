package com.duyanhnguyen.petworld.backend.elasticsearch.repository;

import com.duyanhnguyen.petworld.backend.elasticsearch.document.ESUserDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ESUserRepository extends ElasticsearchRepository<ESUserDocument, Long> {

    @Query("""
    {
      "match_phrase_prefix": {
        "username": {
          "query": "?0"
        }
      }
    }
    """)
    Page<ESUserDocument> searchByUsername(String keyword, Pageable pageable);

}
