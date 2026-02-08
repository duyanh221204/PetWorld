package com.duyanhnguyen.petworld.backend.repository;

import com.duyanhnguyen.petworld.backend.entity.GroupJoinRequestEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GroupJoinRequestRepository extends JpaRepository<GroupJoinRequestEntity, Long> {

    @EntityGraph(attributePaths = {"sender"})
    Page<GroupJoinRequestEntity> findByGroupIdOrderBySubmittedAtAsc(Long groupId, Pageable pageable);

    Optional<GroupJoinRequestEntity> findByIdAndGroupId(Long id, Long groupId);

    Boolean existsByIdAndGroupIdAndSenderId(Long id, Long groupId, Long senderId);

    Optional<GroupJoinRequestEntity> findByIdAndGroupIdAndSenderId(Long id, Long groupId, Long senderId);
    
}
