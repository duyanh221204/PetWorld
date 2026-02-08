package com.duyanhnguyen.petworld.backend.repository;

import com.duyanhnguyen.petworld.backend.entity.ReactionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ReactionRepository extends JpaRepository<ReactionEntity, Long> {

    @EntityGraph(attributePaths = {"sender"})
    Page<ReactionEntity> findByPostIdOrderByCreatedAtDesc(Long postId, Pageable pageable);

    Optional<ReactionEntity> findBySenderIdAndPostId(Long senderId, Long postId);

    Boolean existsBySenderIdAndPostId(Long senderId, Long postId);

    Long countByPostId(Long postId);

    @Query("select r.post.id, count(r) from ReactionEntity r where r.post.id in :postIds group by r.post.id")
    List<Object[]> countByPostIds(@Param("postIds") Collection<Long> postIds);

    @Query("select r.post.id from ReactionEntity r where r.sender.id = :userId and r.post.id in :postIds")
    Set<Long> findPostIdByReactionSenderIdAndPostIdIn(
            @Param("userId") Long userId,
            @Param("postIds") Collection<Long> postIds
    );

}
