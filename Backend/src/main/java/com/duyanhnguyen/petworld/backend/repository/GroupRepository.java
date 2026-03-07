package com.duyanhnguyen.petworld.backend.repository;

import com.duyanhnguyen.petworld.backend.entity.GroupEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<GroupEntity, Long> {

    Boolean existsByName(String name);

    @Query(
            value = "select g from GroupMembershipEntity gm " +
                    "join gm.group g where gm.user.id = :userId " +
                    "and gm.role = com.duyanhnguyen.petworld.backend.enums.GroupRole.OWNER " +
                    "order by g.createdAt desc",
            countQuery = "select count(g) from GroupMembershipEntity gm " +
                    "join gm.group g where gm.user.id = :userId " +
                    "and gm.role = com.duyanhnguyen.petworld.backend.enums.GroupRole.OWNER"
    )
    Page<GroupEntity> findOwnedGroupsByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query(
            value = "select g, gm.role from GroupMembershipEntity gm " +
                    "join gm.group g where gm.user.id = :userId " +
                    "and gm.role <> com.duyanhnguyen.petworld.backend.enums.GroupRole.OWNER " +
                    "order by case " +
                    "when gm.role = com.duyanhnguyen.petworld.backend.enums.GroupRole.ADMIN then 1 " +
                    "when gm.role = com.duyanhnguyen.petworld.backend.enums.GroupRole.MEMBER then 2 " +
                    "else 3 end, gm.joinedAt desc",
            countQuery = "select count(g) from GroupMembershipEntity gm " +
                    "join gm.group g where gm.user.id = :userId " +
                    "and gm.role <> com.duyanhnguyen.petworld.backend.enums.GroupRole.OWNER"
    )
    Page<Object[]> findJoinedGroupsByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query(
            value = "select g from GroupJoinRequestEntity gjr " +
                    "join gjr.group g where gjr.sender.id = :userId " +
                    "order by gjr.submittedAt desc",
            countQuery = "select count(g) from GroupJoinRequestEntity gjr " +
                    "join gjr.group g where gjr.sender.id = :userId"
    )
    Page<GroupEntity> findJoinRequestedGroupsByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query(
            value = "select g from GroupEntity g where not exists (" +
                    "select 1 from GroupMembershipEntity gm where gm.group = g and gm.user.id = :userId) and not exists (" +
                    "select 1 from GroupJoinRequestEntity gjr where gjr.group = g and gjr.sender.id = :userId) " +
                    "order by g.createdAt desc",
            countQuery = "select count(g) from GroupEntity g where not exists (" +
                    "select 1 from GroupMembershipEntity gm where gm.group = g and gm.user.id = :userId) and not exists (" +
                    "select 1 from GroupJoinRequestEntity gjr where gjr.group = g and gjr.sender.id = :userId)"
    )
    Page<GroupEntity> findGroupsNotJoinedOrRequestedByUserId(@Param("userId") Long userId, Pageable pageable);

    List<GroupEntity> findAllByIdIn(Collection<Long> ids);

}
