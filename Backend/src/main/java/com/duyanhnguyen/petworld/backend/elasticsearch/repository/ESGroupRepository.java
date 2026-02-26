package com.duyanhnguyen.petworld.backend.elasticsearch.repository;

import com.duyanhnguyen.petworld.backend.elasticsearch.document.ESGroupDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ESGroupRepository extends ElasticsearchRepository<ESGroupDocument, Long> {

    @Query("""
    {
        "match": {
            "name": {
                "query": "?0",
                "fuzziness": "AUTO"
            }
        }
    }
    """)
    Page<ESGroupDocument> searchByName(String keyword, Pageable pageable);

}
