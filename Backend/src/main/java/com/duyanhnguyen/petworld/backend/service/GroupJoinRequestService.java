package com.duyanhnguyen.petworld.backend.service;

import com.duyanhnguyen.petworld.backend.dto.request.GroupJoinRequestCreateRequest;
import com.duyanhnguyen.petworld.backend.dto.response.GroupJoinRequestResponse;
import com.duyanhnguyen.petworld.backend.dto.response.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface GroupJoinRequestService {

    void createGroupJoinRequest(Long currentUserId, Long groupId, List<GroupJoinRequestCreateRequest> groupJoinRequestCreateRequests);

    Page<GroupJoinRequestResponse> getGroupJoinRequests(Long currentUserId, Long groupId, Pageable pageable);

    PageResponse getRequestPage(Long currentUserId, Long groupId, Long requestId, Integer size);

    void approveGroupJoinRequest(Long currentUserId, Long groupId, Long requestId);

    void rejectGroupJoinRequest(Long currentUserId, Long groupId, Long requestId);

    void cancelGroupJoinRequest(Long currentUserId, Long groupId);

    Long countGroupJoinRequests(Long currentUserId, Long groupId);

}
