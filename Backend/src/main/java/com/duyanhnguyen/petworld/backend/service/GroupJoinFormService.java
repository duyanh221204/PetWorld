package com.duyanhnguyen.petworld.backend.service;

import com.duyanhnguyen.petworld.backend.dto.request.GroupJoinFormRequest;
import com.duyanhnguyen.petworld.backend.dto.response.GroupJoinFormResponse;

import java.util.List;

public interface GroupJoinFormService {

    List<GroupJoinFormResponse> getGroupJoinForms(Long currentUserId, Long groupId);

    GroupJoinFormResponse getActiveGroupJoinForm(Long currentUserId, Long groupId);

    GroupJoinFormResponse createGroupJoinForm(Long currentUserId, Long groupId, GroupJoinFormRequest groupJoinFormRequest);

    GroupJoinFormResponse updateGroupJoinForm(Long currentUserId, Long groupId, Long formId, GroupJoinFormRequest groupJoinFormRequest);

    GroupJoinFormResponse activateGroupJoinForm(Long currentUserId, Long groupId, Long formId);

    void deleteGroupJoinForm(Long currentUserId, Long groupId, Long formId);

}
