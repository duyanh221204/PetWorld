package org.example.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

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
    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    UserGroupEntity group;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    UserEntity user;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "group_join_form_id", referencedColumnName = "id", nullable = false)
    GroupJoinFormEntity groupJoinForm;

    @CreationTimestamp
    @Column(name = "submitted_at", nullable = false)
    LocalDateTime submittedAt;

    @JsonIgnore
    @OneToMany(mappedBy = "groupJoinRequest", cascade = CascadeType.ALL)
    List<GroupJoinRequestAnswerEntity> groupJoinRequestAnswers;

}
