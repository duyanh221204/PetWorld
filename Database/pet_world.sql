CREATE DATABASE pet_world;
USE pet_world;

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(50) NOT NULL UNIQUE,
    hashed_password VARCHAR(255) NOT NULL,
    avatar VARCHAR(255),
    description VARCHAR(50),
    role ENUM('USER', 'ADMIN') NOT NULL,
    is_active BOOLEAN NOT NULL
);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);

CREATE TABLE friendships (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sender_id BIGINT NOT NULL,
    CONSTRAINT fk_friendships_sender
        FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
    recipient_id BIGINT NOT NULL,
    CONSTRAINT fk_friendships_recipient
        FOREIGN KEY (recipient_id) REFERENCES users(id) ON DELETE CASCADE,
    sent_at DATETIME(6) NOT NULL,
    accepted_at DATETIME(6)
);
CREATE INDEX idx_friendships_sender_id ON friendships(sender_id);
CREATE INDEX idx_friendships_recipient_id ON friendships(recipient_id);

CREATE TABLE user_groups (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    created_at DATETIME(6)
);
CREATE INDEX idx_user_groups_name ON user_groups(name);

CREATE TABLE posts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    content TEXT,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    user_id BIGINT NOT NULL,
    CONSTRAINT fk_posts_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    group_id BIGINT,
    CONSTRAINT fk_posts_group
        FOREIGN KEY (group_id) REFERENCES user_groups(id) ON DELETE CASCADE,
    visibility ENUM('PUBLIC', 'FRIENDS_ONLY', 'GROUP_ONLY', 'PRIVATE') NOT NULL
);
CREATE INDEX idx_posts_user_id ON posts(user_id);
CREATE INDEX idx_posts_group_id ON posts(group_id);

CREATE TABLE post_media_resources (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    media_url VARCHAR(255) NOT NULL,
    display_order INT,
    post_id BIGINT NOT NULL,
    CONSTRAINT fk_media_post
        FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE
);
CREATE INDEX idx_post_media_resources_post_id ON post_media_resources(post_id);

CREATE TABLE reactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sender_id BIGINT NOT NULL,
    CONSTRAINT fk_reactions_sender
        FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
    post_id BIGINT NOT NULL,
    CONSTRAINT fk_reactions_post
        FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    created_at DATETIME(6) NOT NULL
);
CREATE INDEX idx_reactions_sender_id ON reactions(sender_id);
CREATE INDEX idx_reactions_post_id ON reactions(post_id);

CREATE TABLE comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    content TEXT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6),
    sender_id BIGINT NOT NULL,
    CONSTRAINT fk_comments_sender
        FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
    post_id BIGINT NOT NULL,
    CONSTRAINT fk_comments_post
        FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    parent_comment_id BIGINT,
    CONSTRAINT fk_comments_parent
        FOREIGN KEY (parent_comment_id) REFERENCES comments(id) ON DELETE CASCADE
);
CREATE INDEX idx_comments_sender_id ON comments(sender_id);
CREATE INDEX idx_comments_post_id ON comments(post_id);

CREATE TABLE group_memberships (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    CONSTRAINT fk_gm_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    group_id BIGINT NOT NULL,
    CONSTRAINT fk_gm_group
        FOREIGN KEY (group_id) REFERENCES user_groups(id) ON DELETE CASCADE,
    role ENUM('OWNER', 'ADMIN', 'MEMBER') NOT NULL,
    joined_at DATETIME(6)
);
CREATE INDEX idx_group_memberships_user_id ON group_memberships(user_id);
CREATE INDEX idx_group_memberships_group_id ON group_memberships(group_id);

CREATE TABLE group_join_forms (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    is_active BOOLEAN NOT NULL,
    group_id BIGINT NOT NULL,
    CONSTRAINT fk_gjf_group
        FOREIGN KEY (group_id) REFERENCES user_groups(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL,
    CONSTRAINT fk_gjf_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
CREATE INDEX idx_group_join_forms_group_id ON group_join_forms(group_id);
CREATE INDEX idx_group_join_forms_user_id ON group_join_forms(user_id);

CREATE TABLE group_join_form_questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    question_text VARCHAR(255) NOT NULL,
    is_required BOOLEAN NOT NULL,
    question_order INT NOT NULL,
    group_join_form_id BIGINT NOT NULL,
    CONSTRAINT fk_gjfq_form
        FOREIGN KEY (group_join_form_id) REFERENCES group_join_forms(id) ON DELETE CASCADE
);
CREATE INDEX idx_group_join_form_questions_form_id ON group_join_form_questions(group_join_form_id);

CREATE TABLE group_join_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_id BIGINT NOT NULL,
    CONSTRAINT fk_gjr_group
        FOREIGN KEY (group_id) REFERENCES user_groups(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL,
    CONSTRAINT fk_gjr_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    group_join_form_id BIGINT NOT NULL,
    CONSTRAINT fk_gjr_form
        FOREIGN KEY (group_join_form_id) REFERENCES group_join_forms(id) ON DELETE CASCADE,
    submitted_at DATETIME(6) NOT NULL
);
CREATE INDEX idx_group_join_requests_group_id ON group_join_requests(group_id);
CREATE INDEX idx_group_join_requests_user_id ON group_join_requests(user_id);
CREATE INDEX idx_group_join_requests_form_id ON group_join_requests(group_join_form_id);

CREATE TABLE group_join_request_answers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_join_request_id BIGINT NOT NULL,
    CONSTRAINT fk_gjra_request
        FOREIGN KEY (group_join_request_id) REFERENCES group_join_requests(id) ON DELETE CASCADE,
    group_join_form_question_id BIGINT NOT NULL,
    CONSTRAINT fk_gjra_question
        FOREIGN KEY (group_join_form_question_id) REFERENCES group_join_form_questions(id) ON DELETE CASCADE,
    answer_text VARCHAR(255)
);
CREATE INDEX idx_group_join_request_answers_request_id ON group_join_request_answers(group_join_request_id);
CREATE INDEX idx_group_join_request_answers_question_id ON group_join_request_answers(group_join_form_question_id);

CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type ENUM(
        'FRIEND_REQUEST_RECEIVED',
        'FRIEND_REQUEST_ACCEPTED',
        'POST_REACTED',
        'POST_COMMENTED',
        'COMMENT_REPLIED',
        'GROUP_JOIN_REQUEST_RECEIVED',
        'GROUP_JOIN_REQUEST_ACCEPTED',
        'GROUP_JOIN_REQUEST_REJECTED'
    ) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    is_read BOOLEAN NOT NULL,
    metadata JSON NOT NULL,
    sender_id BIGINT NOT NULL,
    CONSTRAINT fk_notifications_sender
        FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
    recipient_id BIGINT NOT NULL,
    CONSTRAINT fk_notifications_recipient
        FOREIGN KEY (recipient_id) REFERENCES users(id) ON DELETE CASCADE,
    post_id BIGINT,
    CONSTRAINT fk_notifications_post
        FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    comment_id BIGINT,
    CONSTRAINT fk_notifications_comment
        FOREIGN KEY (comment_id) REFERENCES comments(id) ON DELETE CASCADE,
    friendship_id BIGINT,
    CONSTRAINT fk_notifications_friendship
        FOREIGN KEY (friendship_id) REFERENCES friendships(id) ON DELETE CASCADE,
    group_id BIGINT,
    CONSTRAINT fk_notifications_group
        FOREIGN KEY (group_id) REFERENCES user_groups(id) ON DELETE CASCADE
);
CREATE INDEX idx_notifications_sender_id ON notifications(sender_id);
CREATE INDEX idx_notifications_recipient_id ON notifications(recipient_id);
CREATE INDEX idx_notifications_post_id ON notifications(post_id);
CREATE INDEX idx_notifications_comment_id ON notifications(comment_id);
CREATE INDEX idx_notifications_friendship_id ON notifications(friendship_id);
CREATE INDEX idx_notifications_group_id ON notifications(group_id);

CREATE TABLE conversations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_1_id BIGINT NOT NULL,
    CONSTRAINT fk_conversations_user_1
        FOREIGN KEY (user_1_id) REFERENCES users(id) ON DELETE CASCADE,
    user_2_id BIGINT NOT NULL,
    CONSTRAINT fk_conversations_user_2
        FOREIGN KEY (user_2_id) REFERENCES users(id) ON DELETE CASCADE,
    created_at DATETIME(6) NOT NULL,
    UNIQUE KEY unique_conversation (user_1_id, user_2_id),
    CHECK (user_1_id < user_2_id)
);
CREATE INDEX idx_conversations_user_1_id ON conversations(user_1_id);
CREATE INDEX idx_conversations_user_2_id ON conversations(user_2_id);

CREATE TABLE messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    content TEXT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    is_read BOOLEAN NOT NULL,
    sender_id BIGINT NOT NULL,
    CONSTRAINT fk_messages_sender
        FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
    conversation_id BIGINT NOT NULL,
    CONSTRAINT fk_messages_conversation
        FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE
);
CREATE INDEX idx_messages_sender_id ON messages(sender_id);
CREATE INDEX idx_messages_conversation_id ON messages(conversation_id);

