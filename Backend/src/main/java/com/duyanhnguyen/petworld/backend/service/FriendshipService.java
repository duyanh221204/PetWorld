package com.duyanhnguyen.petworld.backend.service;

import com.duyanhnguyen.petworld.backend.dto.response.FriendshipResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FriendshipService {

    Page<FriendshipResponse> getFriendshipRequests(Long currentUserId, Pageable pageable);

    FriendshipResponse sendFriendRequest(Long currentUserId, Long recipientId);

    FriendshipResponse acceptFriendRequest(Long currentUserId, Long friendshipId);

    void rejectFriendRequest(Long currentUserId, Long friendshipId);

    void cancelFriendRequest(Long currentUserId, Long friendshipId);

    void deleteFriendship(Long currentUserId, Long friendshipId);

}
