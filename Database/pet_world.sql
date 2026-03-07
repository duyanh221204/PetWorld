create database pet_world;
use pet_world;

create table user_groups
(
    id              bigint auto_increment
        primary key,
    cover_image_url varchar(255) null,
    created_at      datetime(6)  not null,
    description     text         null,
    name            varchar(255) not null
);
create index idx_ug_name on user_groups (name);
create index idx_ug_created_at on user_groups (created_at);

create table users
(
    id              bigint auto_increment
        primary key,
    avatar          varchar(255)           null,
    description     varchar(255)           null,
    email           varchar(255)           not null,
    hashed_password varchar(255)           not null,
    is_active       bit                    not null,
    role            enum ('ADMIN', 'USER') not null,
    username        varchar(255)           not null,
    constraint uq_u_email
        unique (email),
    constraint uq_u_username
        unique (username)
);
create index idx_u_username_is_active on users (username, is_active);
create index idx_u_email_is_active on users (email, is_active);

create table chats
(
    id                   bigint auto_increment
        primary key,
    last_message_preview text        not null,
    last_messaged_at     datetime(6) not null,
    last_sender_id       bigint      not null,
    user1_has_unread     bit         not null,
    user2_has_unread     bit         not null,
    user1_id             bigint      not null,
    user2_id             bigint      not null,
    constraint fk_c_user2
        foreign key (user2_id) references users (id),
    constraint fk_c_user1
        foreign key (user1_id) references users (id),
    constraint chk_c_user_order
        check (user1_id < user2_id),
    constraint uq_c_users
        unique (user1_id, user2_id)
);
create index idx_c_user1_last_messaged on chats (user1_id, last_messaged_at);
create index idx_c_user2_last_messaged on chats (user2_id, last_messaged_at);
create index idx_c_user1_unread on chats (user1_id, user1_has_unread);
create index idx_c_user2_unread on chats (user2_id, user2_has_unread);

create table chat_messages
(
    id         bigint auto_increment
        primary key,
    content    text        not null,
    created_at datetime(6) not null,
    is_read    bit         not null,
    chat_id    bigint      not null,
    sender_id  bigint      not null,
    constraint fk_cm_sender
        foreign key (sender_id) references users (id),
    constraint fk_cm_chat
        foreign key (chat_id) references chats (id)
);
create index idx_cm_chat_created on chat_messages (chat_id, created_at);
create index idx_cm_chat_sender_read on chat_messages (chat_id, sender_id, is_read);
create index idx_cm_sender_id on chat_messages (sender_id);

create table friendships
(
    id           bigint auto_increment
        primary key,
    accepted_at  datetime(6) null,
    sent_at      datetime(6) not null,
    recipient_id bigint      not null,
    sender_id    bigint      not null,
    constraint fk_f_recipient
        foreign key (recipient_id) references users (id),
    constraint fk_f_sender
        foreign key (sender_id) references users (id),
    constraint chk_f_self
        check (sender_id <> recipient_id),
    constraint uq_f_sender_recipient
        unique (sender_id, recipient_id)
);
create index idx_f_sender_recipient on friendships (sender_id, recipient_id);
create index idx_f_sender_recipient_accepted_at on friendships (sender_id, recipient_id, accepted_at);
create index idx_f_recipient_accepted_sent on friendships (recipient_id, accepted_at, sent_at);

create table group_join_forms
(
    id         bigint auto_increment
        primary key,
    created_at datetime(6)  not null,
    is_active  bit          not null,
    title      varchar(255) not null,
    creator_id bigint       not null,
    group_id   bigint       not null,
    constraint fk_gjf_group
        foreign key (group_id) references user_groups (id),
    constraint fk_gjf_creator
        foreign key (creator_id) references users (id)
);
create index idx_gjf_group_active on group_join_forms (group_id, is_active);
create index idx_gjf_creator_id on group_join_forms (creator_id);

create table group_join_form_questions
(
    id                 bigint auto_increment
        primary key,
    is_required        bit          not null,
    question_order     int          not null,
    question_text      varchar(255) not null,
    group_join_form_id bigint       not null,
    constraint fk_gjfq_form
        foreign key (group_join_form_id) references group_join_forms (id)
);
create index idx_gjfq_form_order on group_join_form_questions (group_join_form_id, question_order);

create table group_join_requests
(
    id                 bigint auto_increment
        primary key,
    submitted_at       datetime(6) not null,
    group_id           bigint      not null,
    group_join_form_id bigint      null,
    sender_id          bigint      not null,
    constraint fk_gjr_group
        foreign key (group_id) references user_groups (id),
    constraint fk_gjr_sender
        foreign key (sender_id) references users (id),
    constraint fk_gjr_form
        foreign key (group_join_form_id) references group_join_forms (id),
    constraint uq_gjr_sender_group
        unique (sender_id, group_id)
);
create index idx_gjr_group_submitted on group_join_requests (group_id, submitted_at);
create index idx_gjr_group_sender on group_join_requests (group_id, sender_id);
create index idx_gjr_sender_id on group_join_requests (sender_id);
create index idx_gjr_form_id on group_join_requests (group_join_form_id);

create table group_join_request_answers
(
    id                          bigint auto_increment
        primary key,
    answer_text                 varchar(255) null,
    group_join_form_question_id bigint       not null,
    group_join_request_id       bigint       not null,
    constraint fk_gjra_request
        foreign key (group_join_request_id) references group_join_requests (id),
    constraint fk_gjra_question
        foreign key (group_join_form_question_id) references group_join_form_questions (id)
);
create index idx_gjra_request_id on group_join_request_answers (group_join_request_id);
create index idx_gjra_question_id on group_join_request_answers (group_join_form_question_id);

create table group_memberships
(
    id        bigint auto_increment
        primary key,
    joined_at datetime(6)                       not null,
    role      enum ('ADMIN', 'MEMBER', 'OWNER') not null,
    group_id  bigint                            not null,
    user_id   bigint                            not null,
    constraint fk_gm_user
        foreign key (user_id) references users (id),
    constraint fk_gm_group
        foreign key (group_id) references user_groups (id),
    constraint uq_group_membership
        unique (user_id, group_id)
);
create index idx_gm_user_group on group_memberships (user_id, group_id, role);
create index idx_gm_group_role_joined on group_memberships (group_id, role, joined_at);

create table posts
(
    id         bigint auto_increment
        primary key,
    content    text                                                     null,
    created_at datetime(6)                                              not null,
    updated_at datetime(6)                                              null,
    visibility enum ('FRIENDS_ONLY', 'GROUP_ONLY', 'PRIVATE', 'PUBLIC') not null,
    creator_id bigint                                                   not null,
    group_id   bigint                                                   null,
    constraint fk_p_creator
        foreign key (creator_id) references users (id),
    constraint fk_p_group
        foreign key (group_id) references user_groups (id)
);
create index idx_p_visibility_creator_created on posts (visibility, creator_id, created_at);
create index idx_p_creator_created on posts (creator_id, created_at);
create index idx_p_group_created on posts (group_id, created_at);
create index idx_p_visibility_created on posts (visibility, created_at);

create table comments
(
    id                bigint auto_increment
        primary key,
    content           varchar(255) not null,
    created_at        datetime(6)  not null,
    updated_at        datetime(6)  null,
    parent_comment_id bigint       null,
    post_id           bigint       not null,
    sender_id         bigint       not null,
    root_comment_id   bigint       null,
    constraint fk_c_sender
        foreign key (sender_id) references users (id),
    constraint fk_c_parent_comment
        foreign key (parent_comment_id) references comments (id),
    constraint fk_c_root_comment
        foreign key (root_comment_id) references comments (id),
    constraint fk_c_post
        foreign key (post_id) references posts (id)
);
create index idx_c_post_root_created on comments (post_id, root_comment_id, created_at);
create index idx_c_root_created on comments (root_comment_id, created_at);
create index idx_c_sender_id on comments (sender_id);
create index idx_c_parent_id on comments (parent_comment_id);

create table notifications
(
    id                    bigint auto_increment
        primary key,
    created_at            datetime(6)                                                                                                                                                                    not null,
    is_read               bit                                                                                                                                                                            not null,
    type                  enum ('COMMENT_REPLIED', 'FRIEND_REQUEST_ACCEPTED', 'FRIEND_REQUEST_RECEIVED', 'GROUP_JOIN_REQUEST_ACCEPTED', 'GROUP_JOIN_REQUEST_RECEIVED', 'POST_COMMENTED', 'POST_REACTED') not null,
    comment_id            bigint                                                                                                                                                                         null,
    friendship_id         bigint                                                                                                                                                                         null,
    group_id              bigint                                                                                                                                                                         null,
    post_id               bigint                                                                                                                                                                         null,
    recipient_id          bigint                                                                                                                                                                         not null,
    sender_id             bigint                                                                                                                                                                         not null,
    group_join_request_id bigint                                                                                                                                                                         null,
    constraint fk_n_sender
        foreign key (sender_id) references users (id),
    constraint fk_n_post
        foreign key (post_id) references posts (id),
    constraint fk_n_group
        foreign key (group_id) references user_groups (id),
    constraint fk_n_comment
        foreign key (comment_id) references comments (id),
    constraint fk_n_recipient
        foreign key (recipient_id) references users (id),
    constraint fk_n_friendship
        foreign key (friendship_id) references friendships (id),
    constraint fk_n_gjr
        foreign key (group_join_request_id) references group_join_requests (id),
    constraint chk_n_self
        check (sender_id <> recipient_id)
);
create index idx_n_recipient_created on notifications (recipient_id, created_at);
create index idx_n_recipient_read on notifications (recipient_id, is_read);
create index idx_n_post_reaction on notifications (type, post_id, sender_id, recipient_id);
create index idx_n_sender_id on notifications (sender_id);
create index idx_n_post_id on notifications (post_id);
create index idx_n_group_id on notifications (group_id);
create index idx_n_comment_id on notifications (comment_id);
create index idx_n_friendship_id on notifications (friendship_id);
create index idx_n_gjr_id on notifications (group_join_request_id);

create table post_media_resources
(
    id            bigint auto_increment
        primary key,
    display_order int          not null,
    media_url     varchar(255) not null,
    post_id       bigint       not null,
    constraint fk_pmr_post
        foreign key (post_id) references posts (id)
);
create index idx_pmr_post_id on post_media_resources (post_id);

create table reactions
(
    id         bigint auto_increment
        primary key,
    created_at datetime(6) not null,
    post_id    bigint      not null,
    sender_id  bigint      not null,
    constraint fk_r_sender
        foreign key (sender_id) references users (id),
    constraint fk_r_post
        foreign key (post_id) references posts (id)
);
create index idx_r_post_created on reactions (post_id, created_at);
create index idx_r_sender_post on reactions (sender_id, post_id);
