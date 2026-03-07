package com.duyanhnguyen.petworld.backend.elasticsearch.service.impl;

import com.duyanhnguyen.petworld.backend.elasticsearch.document.ESUserDocument;
import com.duyanhnguyen.petworld.backend.elasticsearch.mapper.ESUserMapper;
import com.duyanhnguyen.petworld.backend.elasticsearch.repository.ESUserRepository;
import com.duyanhnguyen.petworld.backend.elasticsearch.service.ESUserService;
import com.duyanhnguyen.petworld.backend.entity.UserEntity;
import com.duyanhnguyen.petworld.backend.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ESUserServiceImpl implements ESUserService {

    ESUserRepository esUserRepository;
    ESUserMapper esUserMapper;
    UserRepository userRepository;

    @Override
    public void index(Long userId) {
        UserEntity userEntity = userRepository.findById(userId).orElse(null);
        if (userEntity != null)
            esUserRepository.save(esUserMapper.toDocument(userEntity));
        else
            esUserRepository.deleteById(userId);
    }

    @Override
    public Page<Long> searchByKeyword(String keyword, Pageable pageable) {
        Sort sort = Sort.by("_score").descending().and(Sort.by("usernameSort").ascending())
                .and(Sort.by("id").descending());
        Pageable esPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        Page<ESUserDocument> esUsersPage = esUserRepository.searchByUsername(keyword, esPageable);
        return esUsersPage.map(ESUserDocument::getId);
    }

}
