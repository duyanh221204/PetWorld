create database pet_world;
use pet_world;

create table chats
(
    id               bigint auto_increment
        primary key,
    avatar           varchar(255)              null,
    created_at       datetime(6)               not null,
    last_messaged_at datetime(6)               not null,
    name             varchar(255)              null,
    chat_type        enum ('GROUP', 'PRIVATE') not null
);

create table user_groups
(
    id              bigint auto_increment
        primary key,
    cover_image_url varchar(255) null,
    created_at      datetime(6)  not null,
    description     text         null,
    name            varchar(255) not null
);

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
    constraint UK6dotkott2kjsp8vw4d0m25fb7
        unique (email),
    constraint UKr43af9ap4edm43mmtq01oddj6
        unique (username)
);

create table chat_messages
(
    id         bigint auto_increment
        primary key,
    content    text        not null,
    created_at datetime(6) not null,
    is_read    bit         not null,
    chat_id    bigint      not null,
    sender_id  bigint      not null,
    constraint FKgiqeap8ays4lf684x7m0r2729
        foreign key (sender_id) references users (id),
    constraint FKt56nsqjwt7t4sian6vts9wg3t
        foreign key (chat_id) references chats (id)
);

create table chat_participants
(
    id        bigint auto_increment
        primary key,
    joined_at datetime(6)              not null,
    role      enum ('ADMIN', 'MEMBER') not null,
    chat_id   bigint                   not null,
    user_id   bigint                   not null,
    constraint FKbhdyxo0ndtbs1t49l28y21rkw
        foreign key (user_id) references users (id),
    constraint FKn4feij8janlba38q59kl2ebgg
        foreign key (chat_id) references chats (id)
);

create table friendships
(
    id           bigint auto_increment
        primary key,
    accepted_at  datetime(6) null,
    sent_at      datetime(6) not null,
    recipient_id bigint      not null,
    sender_id    bigint      not null,
    constraint FK7dbvoqqjm38gke30l9mlh76hc
        foreign key (recipient_id) references users (id),
    constraint FKs7n4v837jm41ijdacqgfe9acw
        foreign key (sender_id) references users (id)
);

create table group_join_forms
(
    id         bigint auto_increment
        primary key,
    created_at datetime(6)  not null,
    is_active  bit          not null,
    title      varchar(255) not null,
    creator_id bigint       not null,
    group_id   bigint       not null,
    constraint FKd90le4jqcjlfbidvvnotgk762
        foreign key (group_id) references user_groups (id),
    constraint FKmtbo5k1dj7twrfolv77vdbwtf
        foreign key (creator_id) references users (id)
);

create table group_join_form_questions
(
    id                 bigint auto_increment
        primary key,
    is_required        bit          not null,
    question_order     int          not null,
    question_text      varchar(255) not null,
    group_join_form_id bigint       not null,
    constraint FK7kewvc6o9krp06gcj2us4kkue
        foreign key (group_join_form_id) references group_join_forms (id)
);

create table group_join_requests
(
    id                 bigint auto_increment
        primary key,
    submitted_at       datetime(6) not null,
    group_id           bigint      not null,
    group_join_form_id bigint      null,
    sender_id          bigint      not null,
    constraint FK8qp0kr397s0w2oeau8hs7ytcn
        foreign key (group_id) references user_groups (id),
    constraint FKe5v7sww4tt8qtaditt2blywb8
        foreign key (sender_id) references users (id),
    constraint FKgrdi5cv9uvpjiv8ku37n0nq0c
        foreign key (group_join_form_id) references group_join_forms (id)
);

create table group_join_request_answers
(
    id                          bigint auto_increment
        primary key,
    answer_text                 varchar(255) null,
    group_join_form_question_id bigint       not null,
    group_join_request_id       bigint       not null,
    constraint FKbni8ky22f7y3vbfka8to2tolg
        foreign key (group_join_request_id) references group_join_requests (id),
    constraint FKcrfsafb4ko59jh0x40fhq7shy
        foreign key (group_join_form_question_id) references group_join_form_questions (id)
);

create table group_memberships
(
    id        bigint auto_increment
        primary key,
    joined_at datetime(6)                       not null,
    role      enum ('ADMIN', 'MEMBER', 'OWNER') not null,
    group_id  bigint                            not null,
    user_id   bigint                            not null,
    constraint FKlq7o99bv8w6paut0ih5yhboia
        foreign key (user_id) references users (id),
    constraint FKovcyar57416ncttjbg293nlev
        foreign key (group_id) references user_groups (id)
);

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
    constraint FKpbdq30fxpf8l0v3j2eyca7odb
        foreign key (creator_id) references users (id),
    constraint FKsswnflfjcm7n5t357nhloykah
        foreign key (group_id) references user_groups (id)
);

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
    constraint FK25gve0aqih6wleb26fr0f34rw
        foreign key (sender_id) references users (id),
    constraint FK7h839m3lkvhbyv3bcdv7sm4fj
        foreign key (parent_comment_id) references comments (id),
    constraint FKh4c7lvsc298whoyd4w9ta25cr
        foreign key (post_id) references posts (id)
);

create table notifications
(
    id            bigint auto_increment
        primary key,
    created_at    datetime(6)                                                                                                                                                                    not null,
    is_read       bit                                                                                                                                                                            not null,
    type          enum ('COMMENT_REPLIED', 'FRIEND_REQUEST_ACCEPTED', 'FRIEND_REQUEST_RECEIVED', 'GROUP_JOIN_REQUEST_ACCEPTED', 'GROUP_JOIN_REQUEST_RECEIVED', 'POST_COMMENTED', 'POST_REACTED') not null,
    comment_id    bigint                                                                                                                                                                         null,
    friendship_id bigint                                                                                                                                                                         null,
    group_id      bigint                                                                                                                                                                         null,
    post_id       bigint                                                                                                                                                                         null,
    recipient_id  bigint                                                                                                                                                                         not null,
    sender_id     bigint                                                                                                                                                                         not null,
    constraint FK13vcnq3ukas06ho1yrbc5lrb5
        foreign key (sender_id) references users (id),
    constraint FK599539lym3mnkbqks0u806eac
        foreign key (post_id) references posts (id),
    constraint FKa6886rk0ufbj3p6694x9k4h5j
        foreign key (group_id) references user_groups (id),
    constraint FKl7p8sj183bxuwg2sq2ltx3cpv
        foreign key (comment_id) references comments (id),
    constraint FKqqnsjxlwleyjbxlmm213jaj3f
        foreign key (recipient_id) references users (id),
    constraint FKrwmftt8hm58wejbt72oq0yrf9
        foreign key (friendship_id) references friendships (id)
);

create table post_media_resources
(
    id            bigint auto_increment
        primary key,
    display_order int          not null,
    media_url     varchar(255) not null,
    post_id       bigint       not null,
    constraint FKc4n65gd4ladhsiqjtlhcds320
        foreign key (post_id) references posts (id)
);

create table reactions
(
    id         bigint auto_increment
        primary key,
    created_at datetime(6) not null,
    post_id    bigint      not null,
    sender_id  bigint      not null,
    constraint FK29fnquogmcgqqgb61k66vrmph
        foreign key (sender_id) references users (id),
    constraint FKh8b4h9wybhu8tc5w11e8t3krc
        foreign key (post_id) references posts (id)
);
