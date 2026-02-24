package com.duyanhnguyen.petworld.backend.enums;

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
    ERROR_UPLOADING_FILE("Error uploading file", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_USERNAME("Username must be 2 - 15 characters long", HttpStatus.BAD_REQUEST),
    INVALID_EMAIL("Invalid email format", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD("Password must be 6 - 20 characters long", HttpStatus.BAD_REQUEST),
    ERROR_SENDING_EMAIL("Error sending email", HttpStatus.INTERNAL_SERVER_ERROR),
    USER_ACTIVATION_FAILED("User activation failed", HttpStatus.BAD_REQUEST),
    USERNAME_ALREADY_EXISTED("Username already existed", HttpStatus.CONFLICT),
    USER_NOT_FOUND("User not found", HttpStatus.NOT_FOUND),
    EMAIL_ALREADY_EXISTED("Email already existed", HttpStatus.CONFLICT),
    POST_UPLOAD_FAILED("Post must have either text content or at least one media file", HttpStatus.BAD_REQUEST),
    POST_VISIBILITY_REQUIRED("Post visibility is required", HttpStatus.BAD_REQUEST),
    INVALID_POST_VISIBILITY("Invalid post visibility", HttpStatus.BAD_REQUEST),
    POST_NOT_FOUND("Post not found", HttpStatus.NOT_FOUND),
    POST_MEDIA_RESOURCE_NOT_FOUND("Post media resource not found", HttpStatus.NOT_FOUND),
    INVALID_GROUP_NAME("Group name must be 5 - 100 characters long", HttpStatus.BAD_REQUEST),
    GROUP_NOT_FOUND("Group not found", HttpStatus.NOT_FOUND),
    GROUP_ALREADY_EXISTS("Group name already exists", HttpStatus.CONFLICT),
    NOTIFICATION_NOT_FOUND("Notification not found", HttpStatus.NOT_FOUND),
    NOTIFICATION_TYPE_REQUIRED("Notification type is required", HttpStatus.BAD_REQUEST),
    RECIPIENT_ID_REQUIRED("Recipient ID is required", HttpStatus.BAD_REQUEST),
    COMMENT_CONTENT_REQUIRED("Comment content is required", HttpStatus.BAD_REQUEST),
    COMMENT_NOT_FOUND("Comment not found", HttpStatus.NOT_FOUND),
    INVALID_PARENT_COMMENT("Parent comment does not belong to the same post", HttpStatus.BAD_REQUEST),
    FRIENDSHIP_NOT_FOUND("Friendship not found", HttpStatus.NOT_FOUND),
    FRIENDSHIP_REQUEST_ALREADY_EXISTS("Friendship request already exists", HttpStatus.CONFLICT),
    INVALID_FRIENDSHIP_REQUEST("Invalid friendship request", HttpStatus.BAD_REQUEST),
    ALREADY_FRIENDS("Already friends with this user", HttpStatus.CONFLICT),
    NOT_FRIENDS("Not friends with this user", HttpStatus.CONFLICT),
    REACTION_NOT_FOUND("Reaction not found", HttpStatus.NOT_FOUND),
    REACTION_ALREADY_EXISTS("Reaction already exists", HttpStatus.CONFLICT),
    GROUP_MEMBERSHIP_NOT_FOUND("Group membership not found", HttpStatus.NOT_FOUND),
    GROUP_JOIN_FORM_TITLE_REQUIRED("Group join form title is required", HttpStatus.BAD_REQUEST),
    GROUP_JOIN_FORM_IS_ACTIVE_REQUIRED("Group join form isActive is required", HttpStatus.BAD_REQUEST),
    GROUP_JOIN_FORM_NOT_FOUND("Group join form not found", HttpStatus.NOT_FOUND),
    GROUP_JOIN_FORM_QUESTION_TEXT_REQUIRED("Group join form question text is required", HttpStatus.BAD_REQUEST),
    GROUP_JOIN_FORM_QUESTION_IS_REQUIRED_REQUIRED("Group join form question isRequired is required", HttpStatus.BAD_REQUEST),
    GROUP_JOIN_FORM_QUESTION_NOT_FOUND("Group join form question not found", HttpStatus.NOT_FOUND),
    GROUP_JOIN_FORM_QUESTION_ID_REQUIRED("Group join form question ID is required", HttpStatus.BAD_REQUEST),
    GROUP_JOIN_FORM_QUESTION_ORDER_REQUIRED("Group join form question order is required", HttpStatus.BAD_REQUEST),
    ALREADY_GROUP_MEMBER("User is already a member of the group", HttpStatus.CONFLICT),
    GROUP_JOIN_FORM_ANSWERS_REQUIRED("Group join form answers are required", HttpStatus.BAD_REQUEST),
    INVALID_GROUP_JOIN_FORM_ANSWERS("Invalid group join form answers", HttpStatus.BAD_REQUEST),
    REQUIRED_GROUP_JOIN_FORM_ANSWER_MISSING("Required group join form answer is missing", HttpStatus.BAD_REQUEST),
    DUPLICATED_GROUP_JOIN_FORM_QUESTIONS("Duplicated group join form questions in the answers", HttpStatus.BAD_REQUEST),
    GROUP_JOIN_REQUEST_NOT_FOUND("Group join request not found", HttpStatus.NOT_FOUND),
    CHAT_NOT_FOUND("Chat not found", HttpStatus.NOT_FOUND),
    CHAT_RECIPIENT_ID_REQUIRED("Chat recipient ID is required", HttpStatus.BAD_REQUEST),
    CHAT_MESSAGE_CONTENT_REQUIRED("Chat message content is required", HttpStatus.BAD_REQUEST),
    INVALID_CHAT_MESSAGE("Invalid chat message", HttpStatus.BAD_REQUEST),
    ;

    String message;
    HttpStatusCode httpStatusCode;

}
