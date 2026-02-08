package com.duyanhnguyen.petworld.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

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
@Table(name = "group_join_form_questions")
public class GroupJoinFormQuestionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Long id;

    @Column(name = "question_text", nullable = false)
    String questionText;

    @Column(name = "is_required", nullable = false)
    Boolean isRequired;

    @Column(name = "question_order", nullable = false)
    Integer questionOrder;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_join_form_id", referencedColumnName = "id", nullable = false)
    GroupJoinFormEntity groupJoinForm;

    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "groupJoinFormQuestion", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<GroupJoinRequestAnswerEntity> groupJoinRequestAnswers = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof GroupJoinFormQuestionEntity that))
            return false;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

}
