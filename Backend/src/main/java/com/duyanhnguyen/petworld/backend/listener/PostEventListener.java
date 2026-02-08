package com.duyanhnguyen.petworld.backend.listener;

import com.duyanhnguyen.petworld.backend.elasticsearch.service.ESPostService;
import com.duyanhnguyen.petworld.backend.event.PostEvent;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PostEventListener {

    ESPostService esPostService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(PostEvent postEvent) {
        esPostService.index(postEvent.getPostId());
    }

}
