package com.duyanhnguyen.petworld.backend.service;

import com.duyanhnguyen.petworld.backend.dto.request.GroupMembershipRequest;
import com.duyanhnguyen.petworld.backend.dto.response.GroupMembershipResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GroupMembershipService {

    Page<GroupMembershipResponse> getGroupMemberships(Long groupId, Pageable pageable);

    GroupMembershipResponse updateGroupMembership(Long currentUserId, Long groupId, Long userId, GroupMembershipRequest groupMembershipRequest);

    void deleteGroupMembership(Long currentUserId, Long groupId, Long userId);

    void leaveGroup(Long currentUserId, Long groupId);

    void transferGroupOwnershipAndLeave(Long currentUserId, Long groupId, Long userId);

    void createGroupMembership(Long userId, Long groupId);

}
