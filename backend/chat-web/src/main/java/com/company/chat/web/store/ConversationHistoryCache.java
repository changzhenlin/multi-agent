package com.company.chat.web.store;

import com.company.chat.api.message.Message;

import java.util.List;
import java.util.Optional;

interface ConversationHistoryCache {

    Optional<List<Message>> read(String sessionId);

    void write(String sessionId, String userId, List<Message> messages);

    void evict(String sessionId);
}
