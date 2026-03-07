package com.duyanhnguyen.petworld.backend.repository;

import com.duyanhnguyen.petworld.backend.entity.GroupJoinRequestEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface GroupJoinRequestRepository extends JpaRepository<GroupJoinRequestEntity, Long> {

    @EntityGraph(attributePaths = {"sender"})
    Page<GroupJoinRequestEntity> findByGroupIdOrderBySubmittedAtAsc(Long groupId, Pageable pageable);

    Optional<GroupJoinRequestEntity> findByIdAndGroupId(Long id, Long groupId);

    Boolean existsByGroupIdAndSenderId(Long groupId, Long senderId);

    Boolean existsByIdAndGroupIdAndSenderId(Long id, Long groupId, Long senderId);

    Boolean existsByGroupJoinFormId(Long groupJoinFormId);

    Optional<GroupJoinRequestEntity> findByGroupIdAndSenderId(Long groupId, Long senderId);

    Long countByGroupId(Long groupId);

    @Query(
            "select count(gjr) from GroupJoinRequestEntity gjr " +
                    "where gjr.group.id = :groupId and (gjr.submittedAt < :submittedAt or (" +
                    "gjr.submittedAt = :submittedAt and gjr.id < :joinRequestId))"
    )
    Long countOlderGroupJoinRequests(
            @Param("groupId") Long groupId,
            @Param("joinRequestId") Long joinRequestId,
            @Param("submittedAt") Instant submittedAt
    );
    
}
