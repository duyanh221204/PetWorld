package com.duyanhnguyen.petworld.backend.controller.rest;

import com.duyanhnguyen.petworld.backend.dto.response.ApiResponse;
import com.duyanhnguyen.petworld.backend.service.UploadService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UploadController {

    UploadService uploadService;

    @PostMapping
    public ApiResponse<String> uploadFile(@RequestParam("file") MultipartFile multipartFile) {
        return ApiResponse.<String>builder()
                .message("File uploaded successfully")
                .data(uploadService.upload(multipartFile))
                .build();
    }

}
