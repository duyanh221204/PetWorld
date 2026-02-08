package com.duyanhnguyen.petworld.backend.service;

import com.duyanhnguyen.petworld.backend.dto.response.GroupJoinRequestAnswerResponse;

import java.util.List;

public interface GroupJoinRequestAnswerService {

    List<GroupJoinRequestAnswerResponse> getAnswers(Long currentUserId, Long groupId, Long requestId);

}
