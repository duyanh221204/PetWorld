package com.duyanhnguyen.petworld.backend.service;

import com.duyanhnguyen.petworld.backend.dto.request.GroupJoinFormCreateRequest;
import com.duyanhnguyen.petworld.backend.dto.request.GroupJoinFormUpdateRequest;
import com.duyanhnguyen.petworld.backend.dto.response.GroupJoinFormResponse;

import java.util.List;

public interface GroupJoinFormService {

    List<GroupJoinFormResponse> getGroupJoinForms(Long currentUserId, Long groupId);

    GroupJoinFormResponse getActiveGroupJoinForm(Long groupId);

    GroupJoinFormResponse createGroupJoinForm(Long currentUserId, Long groupId, GroupJoinFormCreateRequest groupJoinFormCreateRequest);

    GroupJoinFormResponse updateGroupJoinForm(Long currentUserId, Long groupId, Long formId, GroupJoinFormUpdateRequest groupJoinFormUpdateRequest);

    GroupJoinFormResponse activateGroupJoinForm(Long currentUserId, Long groupId, Long formId);

    void deleteGroupJoinForm(Long currentUserId, Long groupId, Long formId);

}
