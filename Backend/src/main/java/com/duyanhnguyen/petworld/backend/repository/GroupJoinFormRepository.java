package com.duyanhnguyen.petworld.backend.repository;

import com.duyanhnguyen.petworld.backend.entity.GroupJoinFormEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupJoinFormRepository extends JpaRepository<GroupJoinFormEntity, Long> {

    @EntityGraph(attributePaths = {"creator"})
    List<GroupJoinFormEntity> findByGroupId(Long groupId);

    @EntityGraph(attributePaths = {"creator"})
    Optional<GroupJoinFormEntity> findByGroupIdAndIsActive(Long groupId, Boolean isActive);

    Long countByGroupId(Long groupId);

    @Modifying
    @Query("update GroupJoinFormEntity gjf set gjf.isActive = false where gjf.group.id = :groupId and gjf.isActive = true")
    void deactivateActiveGroupJoinForm(@Param("groupId") Long groupId);

    Boolean existsByIdAndIsActive(Long id, Boolean isActive);

    Boolean existsByIdAndGroupId(Long id, Long groupId);

    Optional<GroupJoinFormEntity> findByIdAndGroupId(Long id, Long groupId);

}
