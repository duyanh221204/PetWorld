package com.duyanhnguyen.petworld.backend.repository;

import com.duyanhnguyen.petworld.backend.entity.FriendshipEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FriendshipRepository extends JpaRepository<FriendshipEntity, Long> {

    Boolean existsByAcceptedAtIsNullAndSenderIdAndRecipientId(Long senderId, Long recipientId);

    Boolean existsByAcceptedAtIsNotNullAndSenderIdAndRecipientId(Long senderId, Long recipientId);

    @EntityGraph(attributePaths = {"sender"})
    Page<FriendshipEntity> findByRecipientIdAndAcceptedAtIsNullOrderBySentAtDesc(Long recipientId, Pageable pageable);

    Long countByRecipientIdAndAcceptedAtIsNotNull(Long recipientId);

}
