package com.duyanhnguyen.petworld.backend.repository;

import com.duyanhnguyen.petworld.backend.entity.CommentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, Long> {

    @Query("select c.post.id, count(c) from CommentEntity c where c.post.id in :postIds group by c.post.id")
    List<Object[]> countByPostIds(@Param("postIds") Collection<Long> postIds);

    @Query(
            "select count(c) from CommentEntity c " +
                    "where c.post.id = :postId and c.rootComment is null and (" +
                    "c.createdAt > :createdAt or (c.createdAt = :createdAt and c.id > :commentId))"
    )
    Long countNewerComments(
            @Param("postId") Long postId,
            @Param("commentId") Long commentId,
            @Param("createdAt") Instant createdAt
    );

    Long countByPostId(Long postId);

    @EntityGraph(attributePaths = {"sender"})
    Page<CommentEntity> findByPostIdAndRootCommentIsNullOrderByCreatedAtDesc(Long postId, Pageable pageable);

    @EntityGraph(attributePaths = {"sender", "parentComment", "parentComment.sender"})
    List<CommentEntity> findByRootCommentIdOrderByCreatedAtAsc(Long rootCommentId);

    @Query("select c.rootComment.id, count(c) from CommentEntity c where c.rootComment.id in :rootCommentIds group by c.rootComment.id")
    List<Object[]> countRepliesByRootCommentIds(@Param("rootCommentIds") Collection<Long> rootCommentIds);

}
