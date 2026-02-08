package com.duyanhnguyen.petworld.backend.service.impl;

import com.duyanhnguyen.petworld.backend.dto.request.GroupJoinRequestCreateRequest;
import com.duyanhnguyen.petworld.backend.dto.request.NotificationRequest;
import com.duyanhnguyen.petworld.backend.dto.response.GroupJoinRequestResponse;
import com.duyanhnguyen.petworld.backend.entity.GroupJoinFormEntity;
import com.duyanhnguyen.petworld.backend.entity.GroupJoinRequestAnswerEntity;
import com.duyanhnguyen.petworld.backend.entity.GroupJoinRequestEntity;
import com.duyanhnguyen.petworld.backend.enums.ErrorCode;
import com.duyanhnguyen.petworld.backend.enums.GroupRole;
import com.duyanhnguyen.petworld.backend.enums.NotificationType;
import com.duyanhnguyen.petworld.backend.exception.AppException;
import com.duyanhnguyen.petworld.backend.mapper.GroupJoinRequestMapper;
import com.duyanhnguyen.petworld.backend.repository.*;
import com.duyanhnguyen.petworld.backend.service.GroupJoinRequestService;
import com.duyanhnguyen.petworld.backend.service.GroupMembershipService;
import com.duyanhnguyen.petworld.backend.service.NotificationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GroupJoinRequestServiceImpl implements GroupJoinRequestService {

    GroupJoinRequestRepository groupJoinRequestRepository;
    GroupJoinRequestMapper groupJoinRequestMapper;
    GroupJoinFormQuestionRepository groupJoinFormQuestionRepository;
    GroupMembershipRepository groupMembershipRepository;
    GroupJoinFormRepository groupJoinFormRepository;
    GroupRepository groupRepository;
    UserRepository userRepository;
    NotificationService notificationService;
    GroupMembershipService groupMembershipService;

    @Transactional
    @Override
    public void createGroupJoinRequest(Long currentUserId, Long groupId, List<GroupJoinRequestCreateRequest> groupJoinRequestCreateRequests) {
        if (groupMembershipRepository.existsByUserIdAndGroupId(currentUserId, groupId))
            throw new AppException(ErrorCode.ALREADY_GROUP_MEMBER);

        GroupJoinFormEntity groupJoinFormEntity = groupJoinFormRepository.findByGroupIdAndIsActive(groupId, true)
                .orElse(null);
        if (groupJoinFormEntity == null && groupJoinRequestCreateRequests != null && !groupJoinRequestCreateRequests.isEmpty())
            throw new AppException(ErrorCode.GROUP_JOIN_FORM_NOT_FOUND);
        if (groupJoinFormEntity != null && (groupJoinRequestCreateRequests == null || groupJoinRequestCreateRequests.isEmpty()))
            throw new AppException(ErrorCode.GROUP_JOIN_FORM_ANSWERS_REQUIRED);

        if (groupJoinFormEntity != null) {
            Set<Long> answeredQuestionIds = groupJoinRequestCreateRequests.stream()
                    .map(GroupJoinRequestCreateRequest::getQuestionId)
                    .collect(Collectors.toSet());

            if (answeredQuestionIds.size() != groupJoinRequestCreateRequests.size())
                throw new AppException(ErrorCode.DUPLICATED_GROUP_JOIN_FORM_QUESTIONS);
            
            Set<Long> groupJoinFormQuestions = groupJoinFormQuestionRepository
                    .findIdsByGroupJoinFormId(groupJoinFormEntity.getId());
            if (!groupJoinFormQuestions.containsAll(answeredQuestionIds))
                throw new AppException(ErrorCode.INVALID_GROUP_JOIN_FORM_ANSWERS);

            Set<Long> requiredGroupJoinFormQuestions = groupJoinFormQuestionRepository
                    .findIdsByGroupJoinFormIdAndIsRequired(groupJoinFormEntity.getId(), true);
            groupJoinRequestCreateRequests.forEach(
                    request -> {
                        if (requiredGroupJoinFormQuestions.contains(request.getQuestionId())) {
                            if (request.getAnswerText() == null || request.getAnswerText().isBlank())
                                throw new AppException(ErrorCode.REQUIRED_GROUP_JOIN_FORM_ANSWER_MISSING);
                        }
                    }
            );
        }

        GroupJoinRequestEntity groupJoinRequestEntity = GroupJoinRequestEntity.builder()
                .group(
                        groupRepository.findById(groupId)
                                .orElseThrow(() -> new AppException(ErrorCode.GROUP_NOT_FOUND))
                )
                .sender(
                        userRepository.findById(currentUserId)
                                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND))
                )
                .groupJoinForm(groupJoinFormEntity)
                .build();
        if (groupJoinFormEntity != null) {
            Map<Long, String> answers = groupJoinRequestCreateRequests.stream()
                    .collect(Collectors.toMap(
                            GroupJoinRequestCreateRequest::getQuestionId,
                            GroupJoinRequestCreateRequest::getAnswerText
                    ));
            answers.forEach((questionId, answerText) -> {
                if (answerText != null && !answerText.isBlank()) {
                    GroupJoinRequestAnswerEntity groupJoinRequestAnswerEntity = GroupJoinRequestAnswerEntity.builder()
                            .groupJoinRequest(groupJoinRequestEntity)
                            .groupJoinFormQuestion(groupJoinFormQuestionRepository.getReferenceById(questionId))
                            .answerText(answerText)
                            .build();
                    groupJoinRequestEntity.getGroupJoinRequestAnswers().add(groupJoinRequestAnswerEntity);
                }
            });
        }

        groupJoinRequestRepository.save(groupJoinRequestEntity);

        Set<Long> ownerAdminIds = groupMembershipRepository.findUserIdsByGroupIdAndRoleIn(
                groupId, Set.of(GroupRole.OWNER, GroupRole.ADMIN));
        ownerAdminIds.forEach(
                ownerAdminId -> notificationService.sendNotification(
                        currentUserId,
                        NotificationRequest.builder()
                                .type(NotificationType.GROUP_JOIN_REQUEST_RECEIVED)
                                .recipientId(ownerAdminId)
                                .groupId(groupId)
                                .build()
                )
        );
    }

    @Override
    public Page<GroupJoinRequestResponse> getGroupJoinRequests(Long currentUserId, Long groupId, Pageable pageable) {
        if (!groupMembershipRepository.existsByUserIdAndGroupIdAndRole(currentUserId, groupId, GroupRole.OWNER) &&
                !groupMembershipRepository.existsByUserIdAndGroupIdAndRole(currentUserId, groupId, GroupRole.ADMIN))
            throw new AppException(ErrorCode.UNAUTHORIZED);

        Page<GroupJoinRequestEntity> groupJoinRequestsPage = groupJoinRequestRepository
                .findByGroupIdOrderBySubmittedAtAsc(groupId, pageable);
        return groupJoinRequestsPage.map(groupJoinRequestMapper::toResponse);
    }

    @Transactional
    @Override
    public void approveGroupJoinRequest(Long currentUserId, Long groupId, Long requestId) {
        if (!groupMembershipRepository.existsByUserIdAndGroupIdAndRoleIn(
                currentUserId, groupId, Set.of(GroupRole.OWNER, GroupRole.ADMIN)))
            throw new AppException(ErrorCode.UNAUTHORIZED);

        GroupJoinRequestEntity groupJoinRequestEntity = groupJoinRequestRepository.findByIdAndGroupId(requestId, groupId)
                .orElseThrow(() -> new AppException(ErrorCode.GROUP_JOIN_REQUEST_NOT_FOUND));

        groupJoinRequestRepository.delete(groupJoinRequestEntity);
        groupMembershipService.createGroupMembership(groupJoinRequestEntity.getSender().getId(), groupId);

        notificationService.sendNotification(
                groupJoinRequestEntity.getSender().getId(),
                NotificationRequest.builder()
                        .type(NotificationType.GROUP_JOIN_REQUEST_ACCEPTED)
                        .recipientId(groupJoinRequestEntity.getSender().getId())
                        .groupId(groupId)
                        .build()
        );
    }

    @Transactional
    @Override
    public void rejectGroupJoinRequest(Long currentUserId, Long groupId, Long requestId) {
        if (!groupMembershipRepository.existsByUserIdAndGroupIdAndRoleIn(
                currentUserId, groupId, Set.of(GroupRole.OWNER, GroupRole.ADMIN)))
            throw new AppException(ErrorCode.UNAUTHORIZED);

        groupJoinRequestRepository.delete(groupJoinRequestRepository.findByIdAndGroupId(requestId, groupId)
                .orElseThrow(() -> new AppException(ErrorCode.GROUP_JOIN_REQUEST_NOT_FOUND)));
    }

    @Transactional
    @Override
    public void cancelGroupJoinRequest(Long currentUserId, Long groupId, Long requestId) {
        groupJoinRequestRepository.delete(
                groupJoinRequestRepository.findByIdAndGroupIdAndSenderId(requestId, groupId, currentUserId)
                        .orElseThrow(() -> new AppException(ErrorCode.GROUP_JOIN_REQUEST_NOT_FOUND))
        );
    }

}
