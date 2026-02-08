package com.duyanhnguyen.petworld.backend.service;

import org.springframework.web.multipart.MultipartFile;

public interface UploadService {

    String upload(MultipartFile multipartFile);

}
