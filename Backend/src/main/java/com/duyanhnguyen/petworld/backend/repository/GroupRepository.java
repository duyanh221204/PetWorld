package com.duyanhnguyen.petworld.backend.repository;

import com.duyanhnguyen.petworld.backend.entity.GroupEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepository extends JpaRepository<GroupEntity, Long> {

    Boolean existsByName(String name);

    @Query(
            value = "select g from GroupEntity g where exists (" +
                    "select 1 from GroupMembershipEntity gm where gm.group = g and gm.user.id = :userId) " +
                    "order by g.createdAt desc",
            countQuery = "select count(g) from GroupEntity g where exists (" +
                    "select 1 from GroupMembershipEntity gm where gm.group = g and gm.user.id = :userId)"
    )
    Page<GroupEntity> findGroupsJoinedByUserId(Long userId, Pageable pageable);

    @Query(
            value = "select g from GroupEntity g where not exists (" +
                    "select 1 from GroupMembershipEntity gm where gm.group = g and gm.user.id = :userId) " +
                    "order by g.createdAt desc",
            countQuery = "select count(g) from GroupEntity g where not exists (" +
                    "select 1 from GroupMembershipEntity gm where gm.group = g and gm.user.id = :userId)"
    )
    Page<GroupEntity> findGroupsNotJoinedByUserId(Long userId, Pageable pageable);

}
