package com.duyanhnguyen.petworld.backend.elasticsearch.repository;

import com.duyanhnguyen.petworld.backend.elasticsearch.document.ESPostDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ESPostRepository extends ElasticsearchRepository<ESPostDocument, Long> {

    @Query("""
    {
        "match": {
            "content": {
                "query": "?0",
                "fuzziness": "AUTO"
            }
        }
    }
    """)
    Page<ESPostDocument> searchByContent(String keyword, Pageable pageable);

    @Query("""
    {
        "bool": {
            "must": [
                {
                    "match": {
                        "content": {
                            "query": "?0",
                            "fuzziness": "AUTO"
                        }
                    }
                }
            ],
            "filter": [
                {
                    "term": {
                        "groupId": ?1
                    }
                }
            ]
        }
    }
    """)
    Page<ESPostDocument> searchByContentAndGroupId(String keyword, Long groupId, Pageable pageable);

}
