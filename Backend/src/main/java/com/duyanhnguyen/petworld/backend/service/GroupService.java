package com.duyanhnguyen.petworld.backend.service;

import com.duyanhnguyen.petworld.backend.dto.request.GroupRequest;
import com.duyanhnguyen.petworld.backend.dto.response.GroupResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GroupService {

    GroupResponse getGroupById(Long groupId);

    Page<GroupResponse> getGroups(Long currentUserId, Boolean joined, Pageable pageable);

    GroupResponse createGroup(Long currentUserId, GroupRequest groupRequest);

    GroupResponse updateGroup(Long currentUserId, Long groupId, GroupRequest groupRequest);

    void deleteGroup(Long currentUserId, Long groupId);

}
