package org.example.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "group_join_request_answers")
public class GroupJoinRequestAnswerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Long id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "group_join_request_id", referencedColumnName = "id", nullable = false)
    GroupJoinRequestEntity groupJoinRequest;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "group_join_form_question_id", referencedColumnName = "id", nullable = false)
    GroupJoinFormQuestionEntity groupJoinFormQuestion;

    @Column(name = "answer_text")
    String answerText;

}
