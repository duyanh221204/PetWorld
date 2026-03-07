package com.duyanhnguyen.petworld.backend.service;

import com.duyanhnguyen.petworld.backend.dto.request.GroupRequest;
import com.duyanhnguyen.petworld.backend.dto.response.GroupResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GroupService {

    Page<GroupResponse> getOwnedGroups(Long currentUserId, Pageable pageable);

    Page<GroupResponse> getJoinedGroups(Long currentUserId, Pageable pageable);

    Page<GroupResponse> getJoinRequestedGroups(Long currentUserId, Pageable pageable);

    Page<GroupResponse> getGroupsNotJoinedOrRequested(Long currentUserId, Pageable pageable);

    Page<GroupResponse> searchByName(String keyword, Pageable pageable);

    GroupResponse getGroupById(Long currentUserId, Long groupId);

    GroupResponse createGroup(Long currentUserId, GroupRequest groupRequest);

    GroupResponse updateGroup(Long currentUserId, Long groupId, GroupRequest groupRequest);

    void deleteGroup(Long currentUserId, Long groupId);

}
