package com.duyanhnguyen.petworld.backend.repository;

import com.duyanhnguyen.petworld.backend.entity.ChatEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<ChatEntity, Long> {

    @Query(
            value = "select c from ChatEntity c " +
                    "where :userId in (c.user1.id, c.user2.id) " +
                    "order by c.lastMessagedAt desc",
            countQuery = "select count(c) from ChatEntity c " +
                    "where :userId in (c.user1.id, c.user2.id)"
    )
    @EntityGraph(attributePaths = {"user1", "user2"})
    Page<ChatEntity> findByUserId(@Param("userId") Long userId, Pageable pageable);

    Optional<ChatEntity> findByUser1IdAndUser2Id(Long user1Id, Long user2Id);

    @Query(
            "select count(c) from ChatEntity c " +
                    "where (c.user1.id = :userId and c.user1HasUnread = true) or " +
                    "(c.user2.id = :userId and c.user2HasUnread = true)"
    )
    Long countUnreadChatsByUserId(@Param("userId") Long userId);

}
