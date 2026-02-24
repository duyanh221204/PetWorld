package com.duyanhnguyen.petworld.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "chats")
public class ChatEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user1_id", referencedColumnName = "id", nullable = false)
    UserEntity user1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user2_id", referencedColumnName = "id", nullable = false)
    UserEntity user2;

    @Column(name = "last_messaged_at", nullable = false)
    Instant lastMessagedAt;

    @Column(name = "last_message_preview", nullable = false, columnDefinition = "TEXT")
    String lastMessagePreview;

    @Column(name = "last_sender_id", nullable = false)
    Long lastSenderId;

    @Builder.Default
    @Column(name = "user1_has_unread", nullable = false)
    Boolean user1HasUnread = false;

    @Builder.Default
    @Column(name = "user2_has_unread", nullable = false)
    Boolean user2HasUnread = false;

    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<ChatMessageEntity> chatMessages = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ChatEntity that))
            return false;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

}
