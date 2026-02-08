package com.duyanhnguyen.petworld.backend.service;

import com.duyanhnguyen.petworld.backend.dto.response.ReactionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReactionService {

    Page<ReactionResponse> getReactionsByPostId(Long postId, Pageable pageable);

    void createReaction(Long currentUserId, Long postId);

    void deleteReaction(Long currentUserId, Long postId);

}
