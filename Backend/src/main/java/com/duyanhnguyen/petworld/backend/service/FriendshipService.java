package com.duyanhnguyen.petworld.backend.service;

import com.duyanhnguyen.petworld.backend.dto.response.FriendshipRequestResponse;
import com.duyanhnguyen.petworld.backend.dto.response.FriendshipStatusResponse;
import com.duyanhnguyen.petworld.backend.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FriendshipService {

    Page<FriendshipRequestResponse> getFriendshipRequests(Long currentUserId, Pageable pageable);

    Page<UserResponse> getFriendsList(Long currentUserId, Pageable pageable);

    FriendshipRequestResponse sendFriendRequest(Long currentUserId, Long recipientId);

    FriendshipRequestResponse acceptFriendRequest(Long currentUserId, Long friendshipId);

    void rejectFriendRequest(Long currentUserId, Long friendshipId);

    void cancelFriendRequest(Long currentUserId, Long friendshipId);

    void deleteFriendship(Long currentUserId, Long friendshipId);

    FriendshipStatusResponse getFriendshipStatus(Long currentUserId, Long otherUserId);

}
