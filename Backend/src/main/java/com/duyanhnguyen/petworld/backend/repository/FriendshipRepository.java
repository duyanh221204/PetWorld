package com.duyanhnguyen.petworld.backend.repository;

import com.duyanhnguyen.petworld.backend.entity.FriendshipEntity;
import com.duyanhnguyen.petworld.backend.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<FriendshipEntity, Long> {

    Boolean existsByAcceptedAtIsNullAndSenderIdAndRecipientId(Long senderId, Long recipientId);

    Boolean existsByAcceptedAtIsNotNullAndSenderIdAndRecipientId(Long senderId, Long recipientId);

    @EntityGraph(attributePaths = {"sender"})
    Page<FriendshipEntity> findByRecipientIdAndAcceptedAtIsNullOrderBySentAtDesc(Long recipientId, Pageable pageable);

    @Query(
            "select count(f) from FriendshipEntity f " +
                    "where f.acceptedAt is not null and (f.sender.id = :userId or f.recipient.id = :userId)"
    )
    Long countByUserIdAndAcceptedAtIsNotNull(Long userId);

    @Query(
            value = "select u from FriendshipEntity f " +
                    "join UserEntity u on u.id = case when f.sender.id = :userId then f.recipient.id else f.sender.id end " +
                    "where f.acceptedAt is not null and (f.sender.id = :userId or f.recipient.id = :userId) " +
                    "order by f.acceptedAt desc",
            countQuery = "select count(u) from FriendshipEntity f " +
                    "join UserEntity u on u.id = case when f.sender.id = :userId then f.recipient.id else f.sender.id end " +
                    "where f.acceptedAt is not null and (f.sender.id = :userId or f.recipient.id = :userId)"
    )
    Page<UserEntity> findFriendsOfUser(@Param("userId") Long userId, Pageable pageable);

    @Query(
            "select f from FriendshipEntity f " +
                    "where (f.sender.id = :user1Id and f.recipient.id = :user2Id) " +
                    "or (f.sender.id = :user2Id and f.recipient.id = :user1Id)"
    )
    Optional<FriendshipEntity> findBetweenTwoUsers(@Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id);

}
