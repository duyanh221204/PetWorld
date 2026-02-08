package com.duyanhnguyen.petworld.backend.repository;

import com.duyanhnguyen.petworld.backend.entity.GroupMembershipEntity;
import com.duyanhnguyen.petworld.backend.enums.GroupRole;
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
public interface GroupMembershipRepository extends JpaRepository<GroupMembershipEntity, Long> {

    Boolean existsByUserIdAndGroupId(Long userId, Long groupId);

    Boolean existsByUserIdAndGroupIdAndRole(Long userId, Long groupId, GroupRole role);

    Boolean existsByUserIdAndGroupIdAndRoleIn(Long userId, Long groupId, Collection<GroupRole> roles);

    Optional<GroupMembershipEntity> findByUserIdAndGroupId(Long userId, Long groupId);

    Long countByGroupId(Long groupId);

    @EntityGraph(attributePaths = {"user", "group"})
    Page<GroupMembershipEntity> findByGroupId(Long groupId, Pageable pageable);

    @Query("select gm.group.id, count(gm) from GroupMembershipEntity gm where gm.group.id in :groupIds group by gm.group.id")
    List<Object[]> countByGroupIds(@Param("groupIds") Collection<Long> groupIds);

    @Query("select gm.user.id from GroupMembershipEntity gm where gm.group.id = :groupId and gm.role in :roles")
    Set<Long> findUserIdsByGroupIdAndRoleIn(@Param("groupId") Long groupId, @Param("roles") Collection<GroupRole> roles);

}
