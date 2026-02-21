package com.duyanhnguyen.petworld.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.aspectj.weaver.ast.Not;
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
@Table(name = "group_join_requests")
public class GroupJoinRequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    GroupEntity group;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    UserEntity sender;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_join_form_id", referencedColumnName = "id")
    GroupJoinFormEntity groupJoinForm;

    @CreationTimestamp
    @Column(name = "submitted_at", nullable = false)
    Instant submittedAt;

    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "groupJoinRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<GroupJoinRequestAnswerEntity> groupJoinRequestAnswers = new HashSet<>();

    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "groupJoinRequest", cascade = CascadeType.REMOVE, orphanRemoval = true)
    Set<NotificationEntity> notifications = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof GroupJoinRequestEntity that))
            return false;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

}
