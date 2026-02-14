package com.duyanhnguyen.petworld.backend.service.impl;

import com.duyanhnguyen.petworld.backend.dto.request.GroupRequest;
import com.duyanhnguyen.petworld.backend.dto.response.GroupResponse;
import com.duyanhnguyen.petworld.backend.entity.GroupEntity;
import com.duyanhnguyen.petworld.backend.entity.GroupMembershipEntity;
import com.duyanhnguyen.petworld.backend.entity.UserEntity;
import com.duyanhnguyen.petworld.backend.enums.ErrorCode;
import com.duyanhnguyen.petworld.backend.enums.GroupRole;
import com.duyanhnguyen.petworld.backend.exception.AppException;
import com.duyanhnguyen.petworld.backend.mapper.GroupMapper;
import com.duyanhnguyen.petworld.backend.repository.GroupMembershipRepository;
import com.duyanhnguyen.petworld.backend.repository.GroupRepository;
import com.duyanhnguyen.petworld.backend.repository.UserRepository;
import com.duyanhnguyen.petworld.backend.service.GroupService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GroupServiceImpl implements GroupService {

    GroupRepository groupRepository;
    GroupMapper groupMapper;
    UserRepository userRepository;
    GroupMembershipRepository groupMembershipRepository;

    @Override
    public GroupResponse getGroupById(Long groupId) {
        GroupEntity groupEntity = groupRepository.findById(groupId)
                .orElseThrow(() -> new AppException(ErrorCode.GROUP_NOT_FOUND));

        GroupResponse groupResponse = groupMapper.toResponse(groupEntity);
        groupResponse.setMemberCount(groupMembershipRepository.countByGroupId(groupId));
        return groupResponse;
    }

    @Override
    public Page<GroupResponse> getGroups(Long currentUserId, Boolean joined, Pageable pageable) {
        Page<GroupEntity> groupsPage = joined
                ? groupRepository.findGroupsJoinedByUserId(currentUserId, pageable)
                : groupRepository.findGroupsNotJoinedByUserId(currentUserId, pageable);
        if (groupsPage.isEmpty())
            return Page.empty(pageable);

        List<GroupEntity> groupsPageContent = groupsPage.getContent();
        List<Long> groupIds = groupsPageContent.stream().map(GroupEntity::getId).collect(Collectors.toList());

        List<Object[]> members = groupMembershipRepository.countByGroupIds(groupIds);
        Map<Long, Long> memberCounts = members.stream()
                .collect(Collectors.toMap(member -> (Long) member[0], member -> (Long) member[1]));

        List<GroupResponse> groupResponses = groupsPageContent.stream()
                .map(
                        groupEntity -> {
                            GroupResponse groupResponse = groupMapper.toResponse(groupEntity);
                            groupResponse.setMemberCount(memberCounts.get(groupEntity.getId()));
                            return groupResponse;
                        }
                )
                .collect(Collectors.toList());
        return new PageImpl<>(groupResponses, pageable, groupsPage.getTotalElements());
    }

    @Transactional
    @Override
    public GroupResponse createGroup(Long currentUserId, GroupRequest groupRequest) {
        if (groupRepository.existsByName(groupRequest.getName()))
            throw new AppException(ErrorCode.GROUP_ALREADY_EXISTS);

        UserEntity userEntity = userRepository.findById(currentUserId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        GroupEntity groupEntity = groupMapper.toEntity(groupRequest);

        GroupMembershipEntity groupMembershipEntity = GroupMembershipEntity.builder()
                .user(userEntity)
                .group(groupEntity)
                .role(GroupRole.OWNER)
                .build();
        groupEntity.getGroupMemberships().add(groupMembershipEntity);

        GroupResponse groupResponse = groupMapper.toResponse(groupRepository.save(groupEntity));
        groupResponse.setMemberCount(1L);
        return groupResponse;
    }

    @Transactional
    @Override
    public GroupResponse updateGroup(Long currentUserId, Long groupId, GroupRequest groupRequest) {
        GroupEntity groupEntity = groupRepository.findById(groupId)
                .orElseThrow(() -> new AppException(ErrorCode.GROUP_NOT_FOUND));

        GroupMembershipEntity groupMembershipEntity = groupMembershipRepository.findByUserIdAndGroupId(currentUserId, groupId)
                .orElseThrow(() -> new AppException(ErrorCode.GROUP_MEMBERSHIP_NOT_FOUND));
        if (groupMembershipEntity.getRole() == GroupRole.MEMBER)
            throw new AppException(ErrorCode.UNAUTHORIZED);

        groupMapper.update(groupRequest, groupEntity);

        GroupResponse groupResponse = groupMapper.toResponse(groupEntity);
        groupResponse.setMemberCount(groupMembershipRepository.countByGroupId(groupId));
        return groupResponse;
    }

    @Transactional
    @Override
    public void deleteGroup(Long currentUserId, Long groupId) {
        GroupEntity groupEntity = groupRepository.findById(groupId)
                .orElseThrow(() -> new AppException(ErrorCode.GROUP_NOT_FOUND));

        GroupMembershipEntity groupMembershipEntity = groupMembershipRepository.findByUserIdAndGroupId(currentUserId, groupId)
                .orElseThrow(() -> new AppException(ErrorCode.GROUP_MEMBERSHIP_NOT_FOUND));
        if (groupMembershipEntity.getRole() != GroupRole.OWNER)
            throw new AppException(ErrorCode.UNAUTHORIZED);

        groupRepository.delete(groupEntity);
    }

}
