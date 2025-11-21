package org.example.backend.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum ErrorCode {

    UNCATEGORIZED_ERROR("Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_MESSAGE_KEY("Invalid message key", HttpStatus.BAD_REQUEST),
    LOGIN_FAILED("Invalid credentials or user is not activated", HttpStatus.UNAUTHORIZED),
    UNAUTHENTICATED("Could not validate credentials", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED("Permission denied", HttpStatus.FORBIDDEN),
    ERROR_UPLOADING_FILE("Error uploading file", HttpStatus.BAD_REQUEST),
    INVALID_USERNAME("Username must be 2 - 15 characters long", HttpStatus.BAD_REQUEST),
    INVALID_EMAIL("Invalid email format", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD("Password must be 6 - 20 characters long", HttpStatus.BAD_REQUEST),
    ERROR_SENDING_EMAIL("Error sending email", HttpStatus.INTERNAL_SERVER_ERROR),
    USER_ACTIVATION_FAILED("User activation failed", HttpStatus.BAD_REQUEST),;

    String message;
    HttpStatusCode httpStatusCode;

}
