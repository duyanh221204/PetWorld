package org.example.backend.exception;

import lombok.extern.slf4j.Slf4j;
import org.example.backend.dto.response.ApiResponse;
import org.example.backend.enums.ErrorCode;
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
        return buildExceptionResponse(exception.getErrorCode());
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
