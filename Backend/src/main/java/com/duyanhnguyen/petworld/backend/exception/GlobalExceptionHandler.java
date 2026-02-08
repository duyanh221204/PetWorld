package com.duyanhnguyen.petworld.backend.exception;

import com.duyanhnguyen.petworld.backend.dto.response.ApiResponse;
import com.duyanhnguyen.petworld.backend.enums.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;
import java.util.Objects;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(AppException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        if (errorCode == ErrorCode.UNAUTHENTICATED) {
            ResponseCookie delete = ResponseCookie.from("refresh_token", "")
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .maxAge(0)
                    .sameSite("Lax")
                    .build();

            return ResponseEntity.status(errorCode.getHttpStatusCode())
                    .header(HttpHeaders.SET_COOKIE, delete.toString())
                    .body(
                            ApiResponse.<Void>builder()
                                    .status(errorCode.getHttpStatusCode().value())
                                    .message(errorCode.getMessage())
                                    .build()
                    );
        }
        return buildExceptionResponse(errorCode);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException exception) {
        ErrorCode errorCode = ErrorCode.INVALID_MESSAGE_KEY;
        try {
            errorCode = ErrorCode.valueOf(Objects.requireNonNull(exception.getFieldError()).getDefaultMessage());
        } catch (IllegalArgumentException e) {
            log.warn(exception.toString());
        }
        return buildExceptionResponse(errorCode);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(RuntimeException exception) {
        return buildExceptionResponse(ErrorCode.UNCATEGORIZED_ERROR);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException exception) {
        return buildExceptionResponse(ErrorCode.UNAUTHORIZED);
    }

    private ResponseEntity<ApiResponse<Void>> buildExceptionResponse(ErrorCode errorCode) {
        return ResponseEntity.status(errorCode.getHttpStatusCode()).body(
                ApiResponse.<Void>builder()
                        .status(errorCode.getHttpStatusCode().value())
                        .message(errorCode.getMessage())
                        .build()
        );
    }

}
