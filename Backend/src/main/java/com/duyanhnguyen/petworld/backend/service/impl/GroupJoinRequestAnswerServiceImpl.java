package com.duyanhnguyen.petworld.backend.service.impl;

import com.duyanhnguyen.petworld.backend.dto.response.GroupJoinRequestAnswerResponse;
import com.duyanhnguyen.petworld.backend.entity.GroupJoinFormQuestionEntity;
import com.duyanhnguyen.petworld.backend.entity.GroupJoinRequestAnswerEntity;
import com.duyanhnguyen.petworld.backend.entity.GroupJoinRequestEntity;
import com.duyanhnguyen.petworld.backend.enums.ErrorCode;
import com.duyanhnguyen.petworld.backend.enums.GroupRole;
import com.duyanhnguyen.petworld.backend.exception.AppException;
import com.duyanhnguyen.petworld.backend.repository.GroupJoinFormQuestionRepository;
import com.duyanhnguyen.petworld.backend.repository.GroupJoinRequestRepository;
import com.duyanhnguyen.petworld.backend.repository.GroupMembershipRepository;
import com.duyanhnguyen.petworld.backend.service.GroupJoinRequestAnswerService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GroupJoinRequestAnswerServiceImpl implements GroupJoinRequestAnswerService {

    GroupJoinFormQuestionRepository groupJoinFormQuestionRepository;
    GroupMembershipRepository groupMembershipRepository;
    GroupJoinRequestRepository groupJoinRequestRepository;

    @Override
    public List<GroupJoinRequestAnswerResponse> getAnswers(Long currentUserId, Long groupId, Long requestId) {
        if (!groupMembershipRepository.existsByUserIdAndGroupIdAndRoleIn(
                currentUserId, groupId, Set.of(GroupRole.OWNER, GroupRole.ADMIN)) &&
                !groupJoinRequestRepository.existsByIdAndGroupIdAndSenderId(requestId, groupId, currentUserId))
            throw new AppException(ErrorCode.UNAUTHORIZED);

        GroupJoinRequestEntity groupJoinRequestEntity = groupJoinRequestRepository.findByIdAndGroupId(requestId, groupId)
                .orElseThrow(() -> new AppException(ErrorCode.GROUP_JOIN_REQUEST_NOT_FOUND));
        List<Object[]> questionsWithAnswers = groupJoinFormQuestionRepository
                .findQuestionsWithAnswersByGroupJoinFormIdAndGroupJoinRequestId(
                        groupJoinRequestEntity.getGroupJoinForm().getId(),
                        requestId
                );

        return questionsWithAnswers.stream()
                .map(
                        objects -> {
                            GroupJoinFormQuestionEntity question = (GroupJoinFormQuestionEntity) objects[0];
                            GroupJoinRequestAnswerEntity answer = (GroupJoinRequestAnswerEntity) objects[1];
                            return GroupJoinRequestAnswerResponse.builder()
                                    .questionId(question.getId())
                                    .questionText(question.getQuestionText())
                                    .isRequired(question.getIsRequired())
                                    .answerText(answer != null ? answer.getAnswerText() : null)
                                    .build();
                        }
                )
                .collect(Collectors.toList());
    }

}
