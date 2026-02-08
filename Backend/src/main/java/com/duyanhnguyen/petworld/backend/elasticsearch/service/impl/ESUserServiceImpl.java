package com.duyanhnguyen.petworld.backend.elasticsearch.service.impl;

import com.duyanhnguyen.petworld.backend.elasticsearch.mapper.ESUserMapper;
import com.duyanhnguyen.petworld.backend.elasticsearch.repository.ESUserRepository;
import com.duyanhnguyen.petworld.backend.elasticsearch.service.ESUserService;
import com.duyanhnguyen.petworld.backend.entity.UserEntity;
import com.duyanhnguyen.petworld.backend.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
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

}
