package org.example.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

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
    @ManyToOne
    @JoinColumn(name = "group_join_form_id", referencedColumnName = "id", nullable = false)
    GroupJoinFormEntity groupJoinForm;

    @JsonIgnore
    @OneToMany(mappedBy = "groupJoinFormQuestion", cascade = CascadeType.ALL)
    List<GroupJoinRequestAnswerEntity> groupJoinRequestAnswers;

}
