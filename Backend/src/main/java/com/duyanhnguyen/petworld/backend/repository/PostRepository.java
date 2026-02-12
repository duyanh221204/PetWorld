package com.duyanhnguyen.petworld.backend.repository;

import com.duyanhnguyen.petworld.backend.entity.PostEntity;
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
public interface PostRepository extends JpaRepository<PostEntity, Long> {

    @Query(
            value = "select p from PostEntity p " +
                    "where p.creator.id = :currentUserId " +
                    "or p.visibility = com.duyanhnguyen.petworld.backend.enums.PostVisibility.PUBLIC " +
                    "or (p.visibility = com.duyanhnguyen.petworld.backend.enums.PostVisibility.FRIENDS_ONLY and exists(" +
                    "select 1 from FriendshipEntity f " +
                    "where ((f.sender.id = :currentUserId and f.recipient = p.creator) " +
                    "or (f.sender = p.creator and f.recipient.id = :currentUserId)) " +
                    "and f.acceptedAt is not null)) " +
                    "or (p.visibility = com.duyanhnguyen.petworld.backend.enums.PostVisibility.GROUP_ONLY and exists (" +
                    "select 1 from GroupMembershipEntity gm " +
                    "where gm.group = p.group and gm.user.id = :currentUserId)) " +
                    "order by p.createdAt desc",
            countQuery = "select count(p) from PostEntity p " +
                    "where p.creator.id = :currentUserId " +
                    "or p.visibility = com.duyanhnguyen.petworld.backend.enums.PostVisibility.PUBLIC " +
                    "or (p.visibility = com.duyanhnguyen.petworld.backend.enums.PostVisibility.FRIENDS_ONLY and exists(" +
                    "select 1 from FriendshipEntity f " +
                    "where ((f.sender.id = :currentUserId and f.recipient = p.creator) " +
                    "or (f.sender = p.creator and f.recipient.id = :currentUserId)) " +
                    "and f.acceptedAt is not null)) " +
                    "or (p.visibility = com.duyanhnguyen.petworld.backend.enums.PostVisibility.GROUP_ONLY and exists (" +
                    "select 1 from GroupMembershipEntity gm " +
                    "where gm.group = p.group and gm.user.id = :currentUserId))"
    )
    @EntityGraph(attributePaths = {"creator", "group"})
    Page<PostEntity> findAllForNewsFeed(@Param("currentUserId") Long currentUserId, Pageable pageable);

    @Query(
            value = "select p from PostEntity p " +
                    "where p.visibility = com.duyanhnguyen.petworld.backend.enums.PostVisibility.GROUP_ONLY and exists (" +
                    "select 1 from GroupMembershipEntity gm " +
                    "where gm.group = p.group and gm.user.id = :currentUserId) " +
                    "order by p.createdAt desc",
            countQuery = "select count(p) from PostEntity p " +
                    "where p.visibility = com.duyanhnguyen.petworld.backend.enums.PostVisibility.GROUP_ONLY and exists (" +
                    "select 1 from GroupMembershipEntity gm " +
                    "where gm.group = p.group and gm.user.id = :currentUserId)"
    )
    @EntityGraph(attributePaths = {"creator", "group"})
    Page<PostEntity> findGroupsPostsForNewsFeed(@Param("currentUserId") Long currentUserId, Pageable pageable);

    @Query(
            value = "select p from PostEntity p where exists (" +
                    "select 1 from FriendshipEntity f " +
                    "where ((f.sender.id = :currentUserId and f.recipient = p.creator) " +
                    "or (f.sender = p.creator and f.recipient.id = :currentUserId)) " +
                    "and f.acceptedAt is not null) and (" +
                    "p.visibility = com.duyanhnguyen.petworld.backend.enums.PostVisibility.PUBLIC or " +
                    "p.visibility = com.duyanhnguyen.petworld.backend.enums.PostVisibility.FRIENDS_ONLY or (" +
                    "p.visibility = com.duyanhnguyen.petworld.backend.enums.PostVisibility.GROUP_ONLY and exists (" +
                    "select 1 from GroupMembershipEntity gm " +
                    "where gm.group = p.group and gm.user.id = :currentUserId))) " +
                    "order by p.createdAt desc",
            countQuery = "select count(p) from PostEntity p where exists (" +
                    "select 1 from FriendshipEntity f " +
                    "where ((f.sender.id = :currentUserId and f.recipient = p.creator) " +
                    "or (f.sender = p.creator and f.recipient.id = :currentUserId)) " +
                    "and f.acceptedAt is not null) and (" +
                    "p.visibility = com.duyanhnguyen.petworld.backend.enums.PostVisibility.PUBLIC or " +
                    "p.visibility = com.duyanhnguyen.petworld.backend.enums.PostVisibility.FRIENDS_ONLY or (" +
                    "p.visibility = com.duyanhnguyen.petworld.backend.enums.PostVisibility.GROUP_ONLY and exists (" +
                    "select 1 from GroupMembershipEntity gm " +
                    "where gm.group = p.group and gm.user.id = :currentUserId)))"
    )
    @EntityGraph(attributePaths = {"creator", "group"})
    Page<PostEntity> findFriendsPostsForNewsFeed(@Param("currentUserId") Long currentUserId, Pageable pageable);

    @Query(
            value = "select p from PostEntity p where p.creator.id = :creatorId and (:currentUserId = :creatorId or " +
                    "p.visibility = com.duyanhnguyen.petworld.backend.enums.PostVisibility.PUBLIC or " +
                    "(p.visibility = com.duyanhnguyen.petworld.backend.enums.PostVisibility.FRIENDS_ONLY and exists (" +
                    "select 1 from FriendshipEntity f " +
                    "where ((f.sender.id = :currentUserId and f.recipient = p.creator) " +
                    "or (f.sender = p.creator and f.recipient.id = :currentUserId)) " +
                    "and f.acceptedAt is not null))) " +
                    "order by p.createdAt desc",
            countQuery = "select count(p) from PostEntity p where p.creator.id = :creatorId and (:currentUserId = :creatorId or " +
                    "p.visibility = com.duyanhnguyen.petworld.backend.enums.PostVisibility.PUBLIC or " +
                    "(p.visibility = com.duyanhnguyen.petworld.backend.enums.PostVisibility.FRIENDS_ONLY and exists (" +
                    "select 1 from FriendshipEntity f " +
                    "where ((f.sender.id = :currentUserId and f.recipient = p.creator) " +
                    "or (f.sender = p.creator and f.recipient.id = :currentUserId)) " +
                    "and f.acceptedAt is not null)))"
    )
    @EntityGraph(attributePaths = {"creator"})
    Page<PostEntity> findByCreatorIdForProfile(
            @Param("currentUserId") Long currentUserId,
            @Param("creatorId") Long creatorId,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"creator"})
    Page<PostEntity> findByGroupIdOrderByCreatedAtDesc(Long groupId, Pageable pageable);

    @EntityGraph(attributePaths = {"postMediaResources"})
    List<PostEntity> findByIdIn(Collection<Long> ids);

    Long countByCreatorId(Long creatorId);

}
