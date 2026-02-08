package com.duyanhnguyen.petworld.backend.service.impl;

import com.duyanhnguyen.petworld.backend.dto.request.GroupJoinFormQuestionOrderUpdateRequest;
import com.duyanhnguyen.petworld.backend.dto.request.GroupJoinFormQuestionRequest;
import com.duyanhnguyen.petworld.backend.dto.response.GroupJoinFormQuestionResponse;
import com.duyanhnguyen.petworld.backend.entity.GroupJoinFormQuestionEntity;
import com.duyanhnguyen.petworld.backend.enums.ErrorCode;
import com.duyanhnguyen.petworld.backend.enums.GroupRole;
import com.duyanhnguyen.petworld.backend.exception.AppException;
import com.duyanhnguyen.petworld.backend.mapper.GroupJoinFormQuestionMapper;
import com.duyanhnguyen.petworld.backend.repository.GroupJoinFormQuestionRepository;
import com.duyanhnguyen.petworld.backend.repository.GroupJoinFormRepository;
import com.duyanhnguyen.petworld.backend.repository.GroupMembershipRepository;
import com.duyanhnguyen.petworld.backend.service.GroupJoinFormQuestionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GroupJoinFormQuestionServiceImpl implements GroupJoinFormQuestionService {

    GroupJoinFormQuestionRepository groupJoinFormQuestionRepository;
    GroupJoinFormQuestionMapper groupJoinFormQuestionMapper;
    GroupJoinFormRepository groupJoinFormRepository;
    GroupMembershipRepository groupMembershipRepository;

    @Override
    public List<GroupJoinFormQuestionResponse> getGroupJoinFormQuestions(Long currentUserId, Long groupId, Long formId) {
        if (!groupJoinFormRepository.existsByIdAndGroupId(formId, groupId))
            throw new AppException(ErrorCode.GROUP_JOIN_FORM_NOT_FOUND);

        if (!groupMembershipRepository.existsByUserIdAndGroupId(currentUserId, groupId)) {
            if (groupJoinFormRepository.existsByIdAndIsActive(formId, false))
                throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        if (groupMembershipRepository.existsByUserIdAndGroupIdAndRole(currentUserId, groupId, GroupRole.MEMBER))
            throw new AppException(ErrorCode.UNAUTHORIZED);

        return groupJoinFormQuestionMapper.toResponseList(
                groupJoinFormQuestionRepository.findByGroupJoinFormIdOrderByQuestionOrder(formId)
        );
    }

    @Transactional
    @Override
    public GroupJoinFormQuestionResponse createGroupJoinFormQuestion(Long currentUserId, Long groupId, Long formId, GroupJoinFormQuestionRequest groupJoinFormQuestionRequest) {
        if (!groupJoinFormRepository.existsByIdAndGroupId(formId, groupId))
            throw new AppException(ErrorCode.GROUP_JOIN_FORM_NOT_FOUND);

        if (!groupMembershipRepository.existsByUserIdAndGroupIdAndRole(currentUserId, groupId, GroupRole.OWNER))
            throw new AppException(ErrorCode.UNAUTHORIZED);

        GroupJoinFormQuestionEntity groupJoinFormQuestionEntity = groupJoinFormQuestionMapper.toEntity(groupJoinFormQuestionRequest);
        groupJoinFormQuestionEntity.setGroupJoinForm(groupJoinFormRepository.getReferenceById(formId));
        return groupJoinFormQuestionMapper.toResponse(groupJoinFormQuestionRepository.save(groupJoinFormQuestionEntity));
    }

    @Transactional
    @Override
    public GroupJoinFormQuestionResponse updateGroupJoinFormQuestion(Long currentUserId, Long groupId, Long formId, Long questionId, GroupJoinFormQuestionRequest groupJoinFormQuestionRequest) {
        if (!groupJoinFormRepository.existsByIdAndGroupId(formId, groupId))
            throw new AppException(ErrorCode.GROUP_JOIN_FORM_NOT_FOUND);

        if (!groupMembershipRepository.existsByUserIdAndGroupIdAndRole(currentUserId, groupId, GroupRole.OWNER))
            throw new AppException(ErrorCode.UNAUTHORIZED);

        GroupJoinFormQuestionEntity groupJoinFormQuestionEntity = groupJoinFormQuestionRepository.findByIdAndGroupJoinFormId(questionId, formId)
                .orElseThrow(() -> new AppException(ErrorCode.GROUP_JOIN_FORM_QUESTION_NOT_FOUND));
        groupJoinFormQuestionMapper.update(groupJoinFormQuestionRequest, groupJoinFormQuestionEntity);

        return groupJoinFormQuestionMapper.toResponse(groupJoinFormQuestionEntity);
    }

    @Transactional
    @Override
    public void deleteGroupJoinFormQuestion(Long currentUserId, Long groupId, Long formId, Long questionId) {
        if (!groupJoinFormRepository.existsByIdAndGroupId(formId, groupId))
            throw new AppException(ErrorCode.GROUP_JOIN_FORM_NOT_FOUND);

        if (!groupMembershipRepository.existsByUserIdAndGroupIdAndRole(currentUserId, groupId, GroupRole.OWNER))
            throw new AppException(ErrorCode.UNAUTHORIZED);

        groupJoinFormQuestionRepository.delete(
                groupJoinFormQuestionRepository.findByIdAndGroupJoinFormId(questionId, formId)
                        .orElseThrow(() -> new AppException(ErrorCode.GROUP_JOIN_FORM_QUESTION_NOT_FOUND))
        );
    }

    @Transactional
    @Override
    public void updateGroupJoinFormQuestionOrders(Long currentUserId, Long groupId, Long formId, List<GroupJoinFormQuestionOrderUpdateRequest> groupJoinFormQuestionOrderUpdateRequests) {
        if (!groupJoinFormRepository.existsByIdAndGroupId(formId, groupId))
            throw new AppException(ErrorCode.GROUP_JOIN_FORM_NOT_FOUND);

        if (!groupMembershipRepository.existsByUserIdAndGroupIdAndRoleIn(
                currentUserId, groupId, List.of(GroupRole.OWNER, GroupRole.ADMIN)))
            throw new AppException(ErrorCode.UNAUTHORIZED);

        Set<Long> existingQuestionIds = groupJoinFormQuestionRepository.findIdsByGroupJoinFormId(formId);
        groupJoinFormQuestionOrderUpdateRequests.forEach(
                request -> {
                    if (!existingQuestionIds.contains(request.getId()))
                        throw new AppException(ErrorCode.GROUP_JOIN_FORM_QUESTION_NOT_FOUND);
                    groupJoinFormQuestionRepository.updateGroupJoinFormQuestionOrderById(request.getId(), request.getQuestionOrder());
                }
        );
    }

}
