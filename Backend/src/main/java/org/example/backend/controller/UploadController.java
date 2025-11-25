package org.example.backend.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.backend.dto.response.ApiResponse;
import org.example.backend.enums.ErrorCode;
import org.example.backend.exception.AppException;
import org.example.backend.service.UploadService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UploadController {

    UploadService uploadService;

    @PostMapping()
    public ApiResponse<String> uploadFile(@RequestParam("file") MultipartFile multipartFile) {
        try {
            return ApiResponse.<String>builder()
                    .message("File uploaded successfully")
                    .result(uploadService.upload(multipartFile))
                    .build();
        } catch (IOException e) {
            throw new AppException(ErrorCode.ERROR_UPLOADING_FILE);
        }
    }

}
