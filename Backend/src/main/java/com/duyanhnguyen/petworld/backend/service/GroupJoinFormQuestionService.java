package com.duyanhnguyen.petworld.backend.service;

import com.duyanhnguyen.petworld.backend.dto.request.GroupJoinFormQuestionOrderUpdateRequest;
import com.duyanhnguyen.petworld.backend.dto.request.GroupJoinFormQuestionRequest;
import com.duyanhnguyen.petworld.backend.dto.response.GroupJoinFormQuestionResponse;

import java.util.List;

public interface GroupJoinFormQuestionService {

    List<GroupJoinFormQuestionResponse> getGroupJoinFormQuestions(Long currentUserId, Long groupId, Long formId);

    GroupJoinFormQuestionResponse createGroupJoinFormQuestion(Long currentUserId, Long groupId, Long formId, GroupJoinFormQuestionRequest groupJoinFormQuestionRequest);

    GroupJoinFormQuestionResponse updateGroupJoinFormQuestion(Long currentUserId, Long groupId, Long formId, Long questionId, GroupJoinFormQuestionRequest groupJoinFormQuestionRequest);

    void deleteGroupJoinFormQuestion(Long currentUserId, Long groupId, Long formId, Long questionId);

    void updateGroupJoinFormQuestionOrders(Long currentUserId, Long groupId, Long formId, List<GroupJoinFormQuestionOrderUpdateRequest> groupJoinFormQuestionOrderUpdateRequests);

}
