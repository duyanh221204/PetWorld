package com.duyanhnguyen.petworld.backend.repository;

import com.duyanhnguyen.petworld.backend.entity.ChatEntity;
import com.duyanhnguyen.petworld.backend.enums.ChatType;
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
public interface ChatRepository extends JpaRepository<ChatEntity, Long> {

    @Query(
            value = "select c from ChatEntity c where exists (" +
                    "select 1 from ChatParticipantEntity cp where cp.chat = c and cp.user.id = :currentUserId) " +
                    "order by c.lastMessagedAt desc",
            countQuery = "select count(c) from ChatEntity c where exists (" +
                    "select 1 from ChatParticipantEntity cp where cp.chat = c and cp.user.id = :currentUserId)"
    )
    Page<ChatEntity> findByCurrentUserId(@Param("currentUserId") Long currentUserId, Pageable pageable);

    @EntityGraph(attributePaths = {"chatParticipants", "chatParticipants.user"})
    List<ChatEntity> findByIdInAndType(Collection<Long> ids, ChatType type);

    @Query(
            "select (count(c) > 0) from ChatEntity c " +
                    "where c.type = com.duyanhnguyen.petworld.backend.enums.ChatType.PRIVATE and exists (" +
                    "select 1 from ChatParticipantEntity cp where cp.chat = c and cp.user.id = :user1Id) and exists(" +
                    "select 1 from ChatParticipantEntity cp where cp.chat = c and cp.user.id = :user2Id)"
    )
    Boolean existsPrivateChatBetweenUsers(@Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id);

}
