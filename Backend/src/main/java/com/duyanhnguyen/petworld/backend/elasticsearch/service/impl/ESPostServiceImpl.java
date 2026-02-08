package com.duyanhnguyen.petworld.backend.elasticsearch.service.impl;

import com.duyanhnguyen.petworld.backend.elasticsearch.document.ESPostDocument;
import com.duyanhnguyen.petworld.backend.elasticsearch.mapper.ESPostMapper;
import com.duyanhnguyen.petworld.backend.elasticsearch.repository.ESPostRepository;
import com.duyanhnguyen.petworld.backend.elasticsearch.service.ESPostService;
import com.duyanhnguyen.petworld.backend.entity.PostEntity;
import com.duyanhnguyen.petworld.backend.repository.PostRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ESPostServiceImpl implements ESPostService {

    ESPostRepository esPostRepository;
    ESPostMapper esPostMapper;
    PostRepository postRepository;

    @Override
    public void index(Long postId) {
        PostEntity postEntity = postRepository.findById(postId).orElse(null);
        if (postEntity != null)
            esPostRepository.save(esPostMapper.toDocument(postEntity));
        else
            esPostRepository.deleteById(postId);
    }

    @Override
    public List<Long> getPostIdsForNewsFeedByKeyword(String keyword, Pageable pageable) {
        Sort sort = Sort.by("_score").descending().and(Sort.by("createdAt").descending());
        Pageable esPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        Page<ESPostDocument> esPostsPage = esPostRepository.searchByContent(keyword, esPageable);
        return esPostsPage.map(ESPostDocument::getId).getContent();
    }

    @Override
    public List<Long> getPostIdsForGroupByKeyword(String keyword, Long groupId, Pageable pageable) {
        Sort sort = Sort.by("_score").descending().and(Sort.by("createdAt").descending());
        Pageable esPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        Page<ESPostDocument> esPostsPage = esPostRepository.searchByContentAndGroupId(keyword, groupId, esPageable);
        return esPostsPage.map(ESPostDocument::getId).getContent();
    }

}
