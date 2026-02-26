package com.duyanhnguyen.petworld.backend.service.impl;

import com.duyanhnguyen.petworld.backend.dto.request.GroupRequest;
import com.duyanhnguyen.petworld.backend.dto.response.GroupResponse;
import com.duyanhnguyen.petworld.backend.entity.GroupEntity;
import com.duyanhnguyen.petworld.backend.entity.GroupMembershipEntity;
import com.duyanhnguyen.petworld.backend.entity.UserEntity;
import com.duyanhnguyen.petworld.backend.enums.ErrorCode;
import com.duyanhnguyen.petworld.backend.enums.GroupRole;
import com.duyanhnguyen.petworld.backend.event.GroupEvent;
import com.duyanhnguyen.petworld.backend.exception.AppException;
import com.duyanhnguyen.petworld.backend.mapper.GroupMapper;
import com.duyanhnguyen.petworld.backend.repository.GroupJoinRequestRepository;
import com.duyanhnguyen.petworld.backend.repository.GroupMembershipRepository;
import com.duyanhnguyen.petworld.backend.repository.GroupRepository;
import com.duyanhnguyen.petworld.backend.repository.UserRepository;
import com.duyanhnguyen.petworld.backend.service.GroupService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.ApplicationEventPublisher;
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
    GroupJoinRequestRepository groupJoinRequestRepository;
    ApplicationEventPublisher applicationEventPublisher;

    @Override
    public Page<GroupResponse> getOwnedGroups(Long currentUserId, Pageable pageable) {
        Page<GroupEntity> groupsPage = groupRepository.findOwnedGroupsByUserId(currentUserId, pageable);
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
                            groupResponse.setCurrentUserRole(GroupRole.OWNER);
                            groupResponse.setIsRequestedToJoin(false);
                            return groupResponse;
                        }
                )
                .collect(Collectors.toList());
        return new PageImpl<>(groupResponses, pageable, groupsPage.getTotalElements());
    }

    @Override
    public Page<GroupResponse> getJoinedGroups(Long currentUserId, Pageable pageable) {
        Page<Object[]> groupsPage = groupRepository.findJoinedGroupsByUserId(currentUserId, pageable);
        if (groupsPage.isEmpty())
            return Page.empty(pageable);

        List<Object[]> groupsPageContent = groupsPage.getContent();
        List<Long> groupIds = groupsPageContent.stream()
                .map(group -> ((GroupEntity) group[0]).getId())
                .collect(Collectors.toList());

        List<Object[]> members = groupMembershipRepository.countByGroupIds(groupIds);
        Map<Long, Long> memberCounts = members.stream()
                .collect(Collectors.toMap(member -> (Long) member[0], member -> (Long) member[1]));

        List<GroupResponse> groupResponses = groupsPageContent.stream()
                .map(
                        group -> {
                            GroupEntity groupEntity = (GroupEntity) group[0];
                            GroupRole role = (GroupRole) group[1];
                            GroupResponse groupResponse = groupMapper.toResponse(groupEntity);
                            groupResponse.setMemberCount(memberCounts.get(groupEntity.getId()));
                            groupResponse.setCurrentUserRole(role);
                            groupResponse.setIsRequestedToJoin(false);
                            return groupResponse;
                        }
                )
                .collect(Collectors.toList());
        return new PageImpl<>(groupResponses, pageable, groupsPage.getTotalElements());
    }

    @Override
    public Page<GroupResponse> getJoinRequestedGroups(Long currentUserId, Pageable pageable) {
        Page<GroupEntity> groupsPage = groupRepository.findJoinRequestedGroupsByUserId(currentUserId, pageable);
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
                            groupResponse.setCurrentUserRole(null);
                            groupResponse.setIsRequestedToJoin(true);
                            return groupResponse;
                        }
                )
                .collect(Collectors.toList());
        return new PageImpl<>(groupResponses, pageable, groupsPage.getTotalElements());
    }

    @Override
    public Page<GroupResponse> getGroupsNotJoinedOrRequested(Long currentUserId, Pageable pageable) {
        Page<GroupEntity> groupsPage = groupRepository.findGroupsNotJoinedOrRequestedByUserId(currentUserId, pageable);
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
                            groupResponse.setCurrentUserRole(null);
                            groupResponse.setIsRequestedToJoin(false);
                            return groupResponse;
                        }
                )
                .collect(Collectors.toList());
        return new PageImpl<>(groupResponses, pageable, groupsPage.getTotalElements());
    }

    @Override
    public GroupResponse getGroupById(Long currentUserId, Long groupId) {
        GroupEntity groupEntity = groupRepository.findById(groupId)
                .orElseThrow(() -> new AppException(ErrorCode.GROUP_NOT_FOUND));

        GroupResponse groupResponse = groupMapper.toResponse(groupEntity);
        groupResponse.setMemberCount(groupMembershipRepository.countByGroupId(groupId));
        groupResponse.setCurrentUserRole(groupMembershipRepository.findRoleByUserIdAndGroupId(currentUserId, groupId)
                .orElse(null));
        groupResponse.setIsRequestedToJoin(groupJoinRequestRepository.existsByGroupIdAndSenderId(groupId, currentUserId));
        return groupResponse;
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
        groupResponse.setCurrentUserRole(GroupRole.OWNER);
        applicationEventPublisher.publishEvent(new GroupEvent(groupResponse.getId()));
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
        applicationEventPublisher.publishEvent(new GroupEvent(groupId));

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
        applicationEventPublisher.publishEvent(new GroupEvent(groupId));
    }

}
