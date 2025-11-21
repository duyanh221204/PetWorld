package org.example.backend.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.dto.request.VerificationCodeRequest;
import org.example.backend.dto.response.ApiResponse;
import org.example.backend.enums.ErrorCode;
import org.example.backend.exception.AppException;
import org.example.backend.service.EmailService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmailController {

    EmailService emailService;

    @PostMapping("/send-verification-code")
    public ApiResponse<Void> sendVerificationCode(@RequestBody @Valid VerificationCodeRequest request) {
        try {
            emailService.sendVerificationCode(request.getEmail());
            return ApiResponse.<Void>builder()
                    .message("Verification code sent successfully")
                    .build();
        } catch (Exception e) {
            log.error("Error sending email to {}", request.getEmail(), e);
            throw new AppException(ErrorCode.ERROR_SENDING_EMAIL);
        }
    }

}
