package com.duyanhnguyen.petworld.backend.repository;

import com.duyanhnguyen.petworld.backend.entity.ChatMessageEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {

    @EntityGraph(attributePaths = {"chat", "sender"})
    Page<ChatMessageEntity> findByChatIdOrderByCreatedAtDesc(Long chatId, Pageable pageable);

    @Modifying
    @Query(
            "update ChatMessageEntity cm set cm.isRead = true where " +
                    "cm.chat.id = :chatId and cm.sender.id <> :userId and cm.isRead = false"
    )
    void markAsRead(@Param("chatId") Long chatId, @Param("userId") Long userId);

}
