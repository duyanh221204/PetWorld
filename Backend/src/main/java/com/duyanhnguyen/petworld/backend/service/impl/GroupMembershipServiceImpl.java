package com.duyanhnguyen.petworld.backend.service.impl;

import com.duyanhnguyen.petworld.backend.dto.request.GroupMembershipRequest;
import com.duyanhnguyen.petworld.backend.dto.response.GroupMembershipResponse;
import com.duyanhnguyen.petworld.backend.entity.GroupEntity;
import com.duyanhnguyen.petworld.backend.entity.GroupMembershipEntity;
import com.duyanhnguyen.petworld.backend.entity.UserEntity;
import com.duyanhnguyen.petworld.backend.enums.ErrorCode;
import com.duyanhnguyen.petworld.backend.enums.GroupRole;
import com.duyanhnguyen.petworld.backend.exception.AppException;
import com.duyanhnguyen.petworld.backend.mapper.GroupMembershipMapper;
import com.duyanhnguyen.petworld.backend.repository.GroupMembershipRepository;
import com.duyanhnguyen.petworld.backend.repository.GroupRepository;
import com.duyanhnguyen.petworld.backend.repository.UserRepository;
import com.duyanhnguyen.petworld.backend.service.GroupMembershipService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GroupMembershipServiceImpl implements GroupMembershipService {

    GroupMembershipRepository groupMembershipRepository;
    GroupMembershipMapper groupMembershipMapper;
    GroupRepository groupRepository;
    UserRepository userRepository;

    @Override
    public Page<GroupMembershipResponse> getGroupMemberships(Long groupId, Pageable pageable) {
        if (!groupRepository.existsById(groupId))
            throw new AppException(ErrorCode.GROUP_NOT_FOUND);

        Page<GroupMembershipEntity> groupMembershipsPage = groupMembershipRepository.findByGroupId(groupId, pageable);
        return groupMembershipsPage.map(groupMembershipMapper::toResponse);
    }

    @Transactional
    @Override
    public GroupMembershipResponse updateGroupMembership(Long currentUserId, Long groupId, Long userId, GroupMembershipRequest groupMembershipRequest) {
        GroupMembershipEntity groupMembershipEntity = groupMembershipRepository.findByUserIdAndGroupId(currentUserId, groupId)
                .orElseThrow(() -> new AppException(ErrorCode.GROUP_MEMBERSHIP_NOT_FOUND));
        if (groupMembershipEntity.getRole() != GroupRole.OWNER)
            throw new AppException(ErrorCode.UNAUTHORIZED);

        groupMembershipEntity = groupMembershipRepository.findByUserIdAndGroupId(userId, groupId)
                .orElseThrow(() -> new AppException(ErrorCode.GROUP_MEMBERSHIP_NOT_FOUND));
        groupMembershipMapper.update(groupMembershipRequest, groupMembershipEntity);
        return groupMembershipMapper.toResponse(groupMembershipEntity);
    }

    @Transactional
    @Override
    public void deleteGroupMembership(Long currentUserId, Long groupId, Long userId) {
        GroupMembershipEntity groupMembershipEntity = groupMembershipRepository.findByUserIdAndGroupId(currentUserId, groupId)
                .orElseThrow(() -> new AppException(ErrorCode.GROUP_MEMBERSHIP_NOT_FOUND));
        if (groupMembershipEntity.getRole() == GroupRole.MEMBER || currentUserId.equals(userId))
            throw new AppException(ErrorCode.UNAUTHORIZED);

        GroupMembershipEntity toDelete = groupMembershipRepository.findByUserIdAndGroupId(userId, groupId)
                .orElseThrow(() -> new AppException(ErrorCode.GROUP_MEMBERSHIP_NOT_FOUND));
        if (toDelete.getRole() == GroupRole.OWNER ||
                (groupMembershipEntity.getRole() == GroupRole.ADMIN && toDelete.getRole() == GroupRole.ADMIN))
            throw new AppException(ErrorCode.UNAUTHORIZED);

        groupMembershipRepository.delete(toDelete);
    }

    @Transactional
    @Override
    public void leaveGroup(Long currentUserId, Long groupId) {
        GroupEntity groupEntity = groupRepository.findById(groupId)
                .orElseThrow(() -> new AppException(ErrorCode.GROUP_NOT_FOUND));
        GroupMembershipEntity groupMembershipEntity = groupMembershipRepository.findByUserIdAndGroupId(currentUserId, groupId)
                .orElseThrow(() -> new AppException(ErrorCode.GROUP_MEMBERSHIP_NOT_FOUND));

        if (groupMembershipEntity.getRole() == GroupRole.OWNER) {
            if (groupMembershipRepository.countByGroupId(groupId) > 1)
                throw new AppException(ErrorCode.UNAUTHORIZED);
            groupRepository.delete(groupEntity);
        }
        groupMembershipRepository.delete(groupMembershipEntity);
    }

    @Transactional
    @Override
    public void transferGroupOwnershipAndLeave(Long currentUserId, Long groupId, Long userId) {
        GroupMembershipEntity currentOwner = groupMembershipRepository.findByUserIdAndGroupId(currentUserId, groupId)
                .orElseThrow(() -> new AppException(ErrorCode.GROUP_MEMBERSHIP_NOT_FOUND));
        if (currentOwner.getRole() != GroupRole.OWNER || currentUserId.equals(userId))
            throw new AppException(ErrorCode.UNAUTHORIZED);

        GroupMembershipEntity newOwner = groupMembershipRepository.findByUserIdAndGroupId(userId, groupId)
                .orElseThrow(() -> new AppException(ErrorCode.GROUP_MEMBERSHIP_NOT_FOUND));

        groupMembershipRepository.delete(currentOwner);
        newOwner.setRole(GroupRole.OWNER);
    }

    @Transactional
    @Override
    public void createGroupMembership(Long userId, Long groupId) {
        GroupEntity groupEntity = groupRepository.findById(groupId)
                .orElseThrow(() -> new AppException(ErrorCode.GROUP_NOT_FOUND));
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        GroupMembershipEntity groupMembershipEntity = GroupMembershipEntity.builder()
                .group(groupEntity)
                .user(userEntity)
                .role(GroupRole.MEMBER)
                .build();
        groupMembershipRepository.save(groupMembershipEntity);
    }

}
