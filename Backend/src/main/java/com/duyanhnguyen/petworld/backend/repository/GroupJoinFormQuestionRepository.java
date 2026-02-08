package com.duyanhnguyen.petworld.backend.repository;

import com.duyanhnguyen.petworld.backend.entity.GroupJoinFormQuestionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface GroupJoinFormQuestionRepository extends JpaRepository<GroupJoinFormQuestionEntity, Long> {

    List<GroupJoinFormQuestionEntity> findByGroupJoinFormIdOrderByQuestionOrder(Long groupJoinFormId);

    Optional<GroupJoinFormQuestionEntity> findByIdAndGroupJoinFormId(Long id, Long groupJoinFormId);

    @Query("select gjfq.id from GroupJoinFormQuestionEntity gjfq where gjfq.groupJoinForm.id = :groupJoinFormId")
    Set<Long> findIdsByGroupJoinFormId(@Param("groupJoinFormId") Long groupJoinFormId);

    @Modifying
    @Query("update GroupJoinFormQuestionEntity gjfq set gjfq.questionOrder = :questionOrder where gjfq.id = :id")
    void updateGroupJoinFormQuestionOrderById(@Param("id") Long id, @Param("questionOrder") Integer questionOrder);

    @Query("select gjfq.id from GroupJoinFormQuestionEntity gjfq where gjfq.groupJoinForm.id = :groupJoinFormId and gjfq.isRequired = :isRequired")
    Set<Long> findIdsByGroupJoinFormIdAndIsRequired(@Param("groupJoinFormId") Long groupJoinFormId, @Param("isRequired") Boolean isRequired);

    @Query(
            "select gjfq, gjra " +
                    "from GroupJoinFormQuestionEntity gjfq " +
                    "left join GroupJoinRequestAnswerEntity gjra " +
                    "on gjra.groupJoinFormQuestion = gjfq and gjra.groupJoinRequest.id = :groupJoinRequestId " +
                    "where gjfq.groupJoinForm.id = :groupJoinFormId " +
                    "order by gjfq.questionOrder"
    )
    List<Object[]> findQuestionsWithAnswersByGroupJoinFormIdAndGroupJoinRequestId(
            @Param("groupJoinFormId") Long groupJoinFormId,
            @Param("groupJoinRequestId") Long groupJoinRequestId
    );

}
