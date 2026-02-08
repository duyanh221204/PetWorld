package com.duyanhnguyen.petworld.backend.service.impl;

import com.duyanhnguyen.petworld.backend.dto.request.GroupJoinFormCreateRequest;
import com.duyanhnguyen.petworld.backend.dto.request.GroupJoinFormUpdateRequest;
import com.duyanhnguyen.petworld.backend.dto.response.GroupJoinFormResponse;
import com.duyanhnguyen.petworld.backend.entity.GroupJoinFormEntity;
import com.duyanhnguyen.petworld.backend.entity.GroupMembershipEntity;
import com.duyanhnguyen.petworld.backend.enums.ErrorCode;
import com.duyanhnguyen.petworld.backend.enums.GroupRole;
import com.duyanhnguyen.petworld.backend.exception.AppException;
import com.duyanhnguyen.petworld.backend.mapper.GroupJoinFormMapper;
import com.duyanhnguyen.petworld.backend.repository.GroupJoinFormRepository;
import com.duyanhnguyen.petworld.backend.repository.GroupMembershipRepository;
import com.duyanhnguyen.petworld.backend.service.GroupJoinFormService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GroupJoinFormServiceImpl implements GroupJoinFormService {

    GroupJoinFormRepository groupJoinFormRepository;
    GroupJoinFormMapper groupJoinFormMapper;
    GroupMembershipRepository groupMembershipRepository;

    @Override
    public List<GroupJoinFormResponse> getGroupJoinForms(Long currentUserId, Long groupId) {
        GroupMembershipEntity groupMembershipEntity = groupMembershipRepository.findByUserIdAndGroupId(currentUserId, groupId)
                .orElseThrow(() -> new AppException(ErrorCode.GROUP_MEMBERSHIP_NOT_FOUND));
        if (groupMembershipEntity.getRole() == GroupRole.MEMBER)
            throw new AppException(ErrorCode.UNAUTHORIZED);

        return groupJoinFormMapper.toResponseList(groupJoinFormRepository.findByGroupId(groupId));
    }

    @Override
    public GroupJoinFormResponse getActiveGroupJoinForm(Long groupId) {
        GroupJoinFormEntity groupJoinFormEntity = groupJoinFormRepository.findByGroupIdAndIsActive(groupId, true)
                .orElseThrow(() -> new AppException(ErrorCode.GROUP_JOIN_FORM_NOT_FOUND));
        return groupJoinFormMapper.toResponse(groupJoinFormEntity);
    }

    @Transactional
    @Override
    public GroupJoinFormResponse createGroupJoinForm(Long currentUserId, Long groupId, GroupJoinFormCreateRequest groupJoinFormCreateRequest) {
        GroupMembershipEntity groupMembershipEntity = groupMembershipRepository.findByUserIdAndGroupId(currentUserId, groupId)
                .orElseThrow(() -> new AppException(ErrorCode.GROUP_MEMBERSHIP_NOT_FOUND));
        if (groupMembershipEntity.getRole() != GroupRole.OWNER)
            throw new AppException(ErrorCode.UNAUTHORIZED);

        GroupJoinFormEntity groupJoinFormEntity = groupJoinFormMapper.toEntity(groupJoinFormCreateRequest);
        groupJoinFormEntity.setGroup(groupMembershipEntity.getGroup());
        groupJoinFormEntity.setCreator(groupMembershipEntity.getUser());

        if (groupJoinFormRepository.countByGroupId(groupId) > 0 && groupJoinFormCreateRequest.getIsActive())
            groupJoinFormRepository.deactivateActiveGroupJoinForm(groupId);

        return groupJoinFormMapper.toResponse(groupJoinFormRepository.save(groupJoinFormEntity));
    }

    @Transactional
    @Override
    public GroupJoinFormResponse updateGroupJoinForm(Long currentUserId, Long groupId, Long formId, GroupJoinFormUpdateRequest groupJoinFormUpdateRequest) {
        GroupMembershipEntity groupMembershipEntity = groupMembershipRepository.findByUserIdAndGroupId(currentUserId, groupId)
                .orElseThrow(() -> new AppException(ErrorCode.GROUP_MEMBERSHIP_NOT_FOUND));
        if (groupMembershipEntity.getRole() != GroupRole.OWNER)
            throw new AppException(ErrorCode.UNAUTHORIZED);

        GroupJoinFormEntity groupJoinFormEntity = groupJoinFormRepository.findByIdAndGroupId(formId, groupId)
                .orElseThrow(() -> new AppException(ErrorCode.GROUP_JOIN_FORM_NOT_FOUND));
        groupJoinFormMapper.update(groupJoinFormUpdateRequest, groupJoinFormEntity);
        return groupJoinFormMapper.toResponse(groupJoinFormEntity);
    }

    @Transactional
    @Override
    public GroupJoinFormResponse activateGroupJoinForm(Long currentUserId, Long groupId, Long formId) {
        GroupMembershipEntity groupMembershipEntity = groupMembershipRepository.findByUserIdAndGroupId(currentUserId, groupId)
                .orElseThrow(() -> new AppException(ErrorCode.GROUP_MEMBERSHIP_NOT_FOUND));
        if (groupMembershipEntity.getRole() != GroupRole.OWNER)
            throw new AppException(ErrorCode.UNAUTHORIZED);

        GroupJoinFormEntity groupJoinFormEntity = groupJoinFormRepository.findByIdAndGroupId(formId, groupId)
                .orElseThrow(() -> new AppException(ErrorCode.GROUP_JOIN_FORM_NOT_FOUND));
        groupJoinFormRepository.deactivateActiveGroupJoinForm(groupId);
        groupJoinFormEntity.setIsActive(true);

        return groupJoinFormMapper.toResponse(groupJoinFormEntity);
    }

    @Transactional
    @Override
    public void deleteGroupJoinForm(Long currentUserId, Long groupId, Long formId) {
        GroupMembershipEntity groupMembershipEntity = groupMembershipRepository.findByUserIdAndGroupId(currentUserId, groupId)
                .orElseThrow(() -> new AppException(ErrorCode.GROUP_MEMBERSHIP_NOT_FOUND));
        if (groupMembershipEntity.getRole() != GroupRole.OWNER)
            throw new AppException(ErrorCode.UNAUTHORIZED);

        groupJoinFormRepository.delete(groupJoinFormRepository.findByIdAndGroupId(formId, groupId)
                .orElseThrow(() -> new AppException(ErrorCode.GROUP_JOIN_FORM_NOT_FOUND)));
    }

}
