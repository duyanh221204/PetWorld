package com.duyanhnguyen.petworld.backend.service.impl;

import com.duyanhnguyen.petworld.backend.dto.request.PostMediaResourceRequest;
import com.duyanhnguyen.petworld.backend.dto.request.PostRequest;
import com.duyanhnguyen.petworld.backend.dto.response.PostResponse;
import com.duyanhnguyen.petworld.backend.entity.GroupEntity;
import com.duyanhnguyen.petworld.backend.entity.PostEntity;
import com.duyanhnguyen.petworld.backend.entity.PostMediaResourceEntity;
import com.duyanhnguyen.petworld.backend.enums.ErrorCode;
import com.duyanhnguyen.petworld.backend.enums.PostVisibility;
import com.duyanhnguyen.petworld.backend.event.PostEvent;
import com.duyanhnguyen.petworld.backend.exception.AppException;
import com.duyanhnguyen.petworld.backend.mapper.PostMapper;
import com.duyanhnguyen.petworld.backend.mapper.PostMediaResourceMapper;
import com.duyanhnguyen.petworld.backend.repository.*;
import com.duyanhnguyen.petworld.backend.service.PostService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PostServiceImpl implements PostService {

    PostRepository postRepository;
    PostMapper postMapper;
    PostMediaResourceMapper postMediaResourceMapper;
    UserRepository userRepository;
    ReactionRepository reactionRepository;
    CommentRepository commentRepository;
    GroupRepository groupRepository;
    GroupMembershipRepository groupMembershipRepository;
    ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    @Override
    public PostResponse createPost(Long currentUserId, PostRequest postRequest) {
        if ((postRequest.getContent() == null || postRequest.getContent().isBlank()) &&
                (postRequest.getPostMediaResources() == null || postRequest.getPostMediaResources().isEmpty()))
            throw new AppException(ErrorCode.POST_UPLOAD_FAILED);

        PostEntity postEntity = postMapper.toEntity(postRequest);
        postEntity.setCreator(
                userRepository.findById(currentUserId)
                        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND))
        );
        if (postRequest.getGroupId() != null) {
            if (postRequest.getVisibility() != PostVisibility.GROUP_ONLY)
                throw new AppException(ErrorCode.INVALID_POST_VISIBILITY);

            GroupEntity groupEntity = groupRepository.findById(postRequest.getGroupId())
                    .orElseThrow(() -> new AppException(ErrorCode.GROUP_NOT_FOUND));
            if (!groupMembershipRepository.existsByUserIdAndGroupId(currentUserId, postRequest.getGroupId()))
                throw new AppException(ErrorCode.UNAUTHORIZED);

            postEntity.setGroup(groupEntity);
        }

        if (postEntity.getPostMediaResources() != null && !postEntity.getPostMediaResources().isEmpty())
            postEntity.getPostMediaResources().forEach(media -> media.setPost(postEntity));

        PostEntity toSave = postRepository.save(postEntity);
        applicationEventPublisher.publishEvent(new PostEvent(toSave.getId()));

        PostResponse postResponse = postMapper.toResponse(toSave);

        postResponse.setReactionCount(0L);
        postResponse.setCommentCount(0L);
        postResponse.setIsReactedByCurrentUser(false);

        return postResponse;
    }

    @Transactional(readOnly = true)
    @Override
    public Page<PostResponse> getPostsForNewsFeed(Long currentUserId, Pageable pageable) {
        Page<PostEntity> postsPage = postRepository.findAllForNewsFeed(currentUserId, pageable);
        return buildPostResponsePage(postsPage, currentUserId, pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<PostResponse> getGroupsPostsForNewsFeed(Long currentUserId, Pageable pageable) {
        Page<PostEntity> postsPage = postRepository.findGroupsPostsForNewsFeed(currentUserId, pageable);
        return buildPostResponsePage(postsPage, currentUserId, pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<PostResponse> getFriendsPostsForNewsFeed(Long currentUserId, Pageable pageable) {
        Page<PostEntity> postsPage = postRepository.findFriendsPostsForNewsFeed(currentUserId, pageable);
        return buildPostResponsePage(postsPage, currentUserId, pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<PostResponse> getPostsByGroupId(Long currentUserId, Long groupId, Pageable pageable) {
        if (!groupRepository.existsById(groupId))
            throw new AppException(ErrorCode.GROUP_NOT_FOUND);
        if (!groupMembershipRepository.existsByUserIdAndGroupId(currentUserId, groupId))
            throw new AppException(ErrorCode.UNAUTHORIZED);

        Page<PostEntity> postsPage = postRepository.findByGroupIdOrderByCreatedAtDesc(groupId, pageable);
        return buildPostResponsePage(postsPage, currentUserId, pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<PostResponse> getPostsByUserIdForProfile(Long currentUserId, Long userId, Pageable pageable) {
        if (!userRepository.existsById(userId))
            throw new AppException(ErrorCode.USER_NOT_FOUND);

        Page<PostEntity> postsPage = postRepository.findByCreatorIdForProfile(currentUserId, userId, pageable);
        return buildPostResponsePage(postsPage, currentUserId, pageable);
    }

    private Page<PostResponse> buildPostResponsePage(Page<PostEntity> postsPage, Long currentUserId, Pageable pageable) {
        if (postsPage.isEmpty())
            return Page.empty(pageable);

        List<PostEntity> postsPageContent = postsPage.getContent();
        List<Long> postIds = postsPageContent.stream().map(PostEntity::getId).collect(Collectors.toList());

        Map<Long, PostEntity> posts = postsPageContent.stream()
                .collect(Collectors.toMap(PostEntity::getId, Function.identity()));

        List<PostEntity> postsWithMedia = postRepository.findByIdIn(postIds);
        postsWithMedia.forEach(
                postWithMedia -> {
                    PostEntity postEntity = posts.get(postWithMedia.getId());
                    postEntity.setPostMediaResources(postWithMedia.getPostMediaResources());
                }
        );

        List<Object[]> reactions = reactionRepository.countByPostIds(postIds);
        Map<Long, Long> reactionCounts = reactions.stream()
                .collect(Collectors.toMap(reaction -> (Long) reaction[0], reaction -> (Long) reaction[1]));

        List<Object[]> comments = commentRepository.countByPostIds(postIds);
        Map<Long, Long> commentCounts = comments.stream()
                .collect(Collectors.toMap(comment -> (Long) comment[0], comment -> (Long) comment[1]));

        Set<Long> reactedPostIds = reactionRepository.findPostIdByReactionSenderIdAndPostIdIn(currentUserId, postIds);

        List<PostResponse> postResponses = postsPageContent.stream()
                .map(
                        postEntity -> {
                            PostEntity post = posts.get(postEntity.getId());
                            PostResponse postResponse = postMapper.toResponse(post);

                            postResponse.setReactionCount(reactionCounts.getOrDefault(post.getId(), 0L));
                            postResponse.setCommentCount(commentCounts.getOrDefault(post.getId(), 0L));

                            postResponse.setIsReactedByCurrentUser(reactedPostIds.contains(post.getId()));

                            return postResponse;
                        }
                )
                .collect(Collectors.toList());

        return new PageImpl<>(postResponses, pageable, postsPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    @Override
    public PostResponse getPostById(Long currentUserId, Long postId) {
        PostEntity postEntity = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

        PostResponse postResponse = postMapper.toResponse(postEntity);
        postResponse.setReactionCount(reactionRepository.countByPostId(postId));
        postResponse.setCommentCount(commentRepository.countByPostId(postId));
        postResponse.setIsReactedByCurrentUser(reactionRepository.existsBySenderIdAndPostId(currentUserId, postId));

        return postResponse;
    }

    @Transactional
    @Override
    public PostResponse updatePost(Long currentUserId, Long postId, PostRequest postRequest) {
        PostEntity postEntity = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

        if (!postEntity.getCreator().getId().equals(currentUserId))
            throw new AppException(ErrorCode.UNAUTHORIZED);
        if ((postRequest.getContent() == null || postRequest.getContent().isBlank()) &&
                (postRequest.getPostMediaResources() == null || postRequest.getPostMediaResources().isEmpty()))
            throw new AppException(ErrorCode.POST_UPLOAD_FAILED);

        postMapper.update(postRequest, postEntity);
        if (postRequest.getPostMediaResources() != null) {
            List<PostMediaResourceEntity> currentMedia = postEntity.getPostMediaResources();
            List<PostMediaResourceRequest> requestMedia = postRequest.getPostMediaResources();

            Set<Long> requestMediaIds = requestMedia.stream()
                    .map(PostMediaResourceRequest::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            currentMedia.removeIf(media -> !requestMediaIds.contains(media.getId()));

            Map<Long, PostMediaResourceEntity> existingMedia = currentMedia.stream()
                    .collect(Collectors.toMap(PostMediaResourceEntity::getId, Function.identity()));
            requestMedia.forEach(
                    media -> {
                        PostMediaResourceEntity postMediaResourceEntity;
                        if (media.getId() != null) {
                            postMediaResourceEntity = existingMedia.getOrDefault(media.getId(), null);
                            if (postMediaResourceEntity == null)
                                throw new AppException(ErrorCode.POST_MEDIA_RESOURCE_NOT_FOUND);
                            postMediaResourceMapper.update(media, postMediaResourceEntity);
                        } else {
                            postMediaResourceEntity = postMediaResourceMapper.toEntity(media);
                            postMediaResourceEntity.setPost(postEntity);
                            currentMedia.add(postMediaResourceEntity);
                        }
                    }
            );
        }
        postEntity.setUpdatedAt(Instant.now());
        postEntity.getPostMediaResources().sort(Comparator.comparing(PostMediaResourceEntity::getDisplayOrder));
        applicationEventPublisher.publishEvent(new PostEvent(postId));

        PostResponse postResponse = postMapper.toResponse(postEntity);

        postResponse.setReactionCount(reactionRepository.countByPostId(postId));
        postResponse.setCommentCount(commentRepository.countByPostId(postId));
        postResponse.setIsReactedByCurrentUser(reactionRepository.existsBySenderIdAndPostId(currentUserId, postId));

        return postResponse;
    }

    @Transactional
    @Override
    public void deletePost(Long currentUserId, Long postId) {
        PostEntity postEntity = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));
        if (!postEntity.getCreator().getId().equals(currentUserId) &&
                (postEntity.getGroup() == null || !groupMembershipRepository.existsByUserIdAndGroupId(currentUserId, postEntity.getGroup().getId())))
            throw new AppException(ErrorCode.UNAUTHORIZED);
        postRepository.delete(postEntity);
        applicationEventPublisher.publishEvent(new PostEvent(postId));
    }

}
