package com.duyanhnguyen.petworld.backend.repository;

import com.duyanhnguyen.petworld.backend.entity.GroupJoinRequestEntity;
import com.duyanhnguyen.petworld.backend.entity.NotificationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

    @Override
    @NonNull
    @EntityGraph(attributePaths = {"sender", "recipient", "post", "comment", "comment.rootComment", "friendship", "group"})
    Optional<NotificationEntity> findById(@NonNull Long id);

    @EntityGraph(attributePaths = {"sender", "recipient", "post", "comment", "comment.rootComment", "friendship", "group"})
    Page<NotificationEntity> findByRecipientIdOrderByCreatedAtDesc(Long recipientId, Pageable pageable);

    Long countByRecipientIdAndIsRead(Long recipientId, Boolean isRead);

    @Modifying
    @Query("update NotificationEntity n set n.isRead = true where n.recipient.id = :recipientId and n.isRead = false")
    Long markAllAsRead(@Param("recipientId") Long recipientId);

    @Modifying
    @Query(
            "delete from NotificationEntity n " +
                    "where n.type = com.duyanhnguyen.petworld.backend.enums.NotificationType.POST_REACTED " +
                    "and n.post.id = :postId and n.sender.id = :senderId and n.recipient.id = :recipientId"
    )
    void deletePostReactionNotification(
            @Param("postId") Long postId,
            @Param("senderId") Long senderId,
            @Param("recipientId") Long recipientId
    );

}
