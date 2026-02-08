package com.duyanhnguyen.petworld.backend.repository;

import com.duyanhnguyen.petworld.backend.entity.CommentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, Long> {

    @Query("select c.post.id, count(c) from CommentEntity c where c.post.id in :postIds group by c.post.id")
    List<Object[]> countByPostIds(@Param("postIds") Collection<Long> postIds);

    Long countByPostId(Long postId);

    @EntityGraph(attributePaths = {"sender", "post"})
    Page<CommentEntity> findByPostIdAndParentCommentIsNullOrderByCreatedAtDesc(Long postId, Pageable pageable);

    @EntityGraph(attributePaths = {"sender", "post"})
    List<CommentEntity> findByParentCommentIdOrderByCreatedAtAsc(Long parentCommentId);

    @Query("select c.parentComment.id, count(c) from CommentEntity c where c.parentComment.id in :parentCommentIds group by c.parentComment.id")
    List<Object[]> countRepliesByParentCommentIds(@Param("parentCommentIds") Collection<Long> parentCommentIds);

}
