package com.duyanhnguyen.petworld.backend.event;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.Collection;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GroupChatCreateEvent {

    Long chatId;

    Collection<Long> participantId;

}
