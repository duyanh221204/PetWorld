package com.duyanhnguyen.petworld.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import com.duyanhnguyen.petworld.backend.enums.Role;

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
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Long id;

    @Column(name = "username", nullable = false, unique = true)
    String username;

    @Column(name = "email", nullable = false, unique = true)
    String email;

    @JsonIgnore
    @Column(name = "hashed_password", nullable = false)
    String hashedPassword;

    @Column(name = "avatar")
    String avatar;

    @Column(name = "description")
    String description;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "role", nullable = false)
    Role role = Role.USER;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    Boolean isActive = false;

    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "creator", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<PostEntity> posts = new HashSet<>();

    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<CommentEntity> comments = new HashSet<>();

    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<ReactionEntity> reactions = new HashSet<>();

    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<FriendshipEntity> sentFriendRequests = new HashSet<>();

    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "recipient", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<FriendshipEntity> friendRequests = new HashSet<>();

    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<GroupMembershipEntity> groupMemberships = new HashSet<>();

    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<GroupJoinRequestEntity> groupJoinRequests = new HashSet<>();

    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "creator", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<GroupJoinFormEntity> groupJoinForms = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof UserEntity that))
            return false;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

}
