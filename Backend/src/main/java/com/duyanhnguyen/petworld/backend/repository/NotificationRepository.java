package com.duyanhnguyen.petworld.backend.repository;

import com.duyanhnguyen.petworld.backend.entity.NotificationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

    @EntityGraph(attributePaths = {"sender", "recipient", "post", "comment", "friendship", "group"})
    Page<NotificationEntity> findByRecipientIdOrderByCreatedAtDesc(Long recipientId, Pageable pageable);

    Long countByRecipientIdAndIsRead(Long recipientId, Boolean isRead);

    @Modifying
    @Query("update NotificationEntity n set n.isRead = true where n.recipient.id = :recipientId and n.isRead = false")
    Long markAllAsRead(@Param("recipientId") Long recipientId);

}
