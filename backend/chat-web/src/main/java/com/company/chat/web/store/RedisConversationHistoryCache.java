package com.company.chat.web.store;

import com.company.chat.api.message.Message;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@ConditionalOnBean(StringRedisTemplate.class)
class RedisConversationHistoryCache implements ConversationHistoryCache {

    private static final Duration CONTEXT_TTL = Duration.ofHours(24);
    private static final TypeReference<List<Message>> MESSAGE_LIST = new TypeReference<>() {
    };

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    RedisConversationHistoryCache(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<List<Message>> read(String sessionId) {
        try {
            String messagesJson = (String) redisTemplate.opsForHash().get(key(sessionId), "messages_json");
            if (messagesJson == null || messagesJson.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(messagesJson, MESSAGE_LIST));
        } catch (Exception exception) {
            return Optional.empty();
        }
    }

    @Override
    public void write(String sessionId, String userId, List<Message> messages) {
        try {
            redisTemplate.opsForHash().putAll(key(sessionId), Map.of(
                    "session_id", sessionId,
                    "user_id", userId,
                    "messages_json", objectMapper.writeValueAsString(messages),
                    "updated_at", OffsetDateTime.now().toString()));
            redisTemplate.expire(key(sessionId), CONTEXT_TTL);
        } catch (Exception exception) {
            // Cache failures must not break chat completion or persistence.
        }
    }

    @Override
    public void evict(String sessionId) {
        try {
            redisTemplate.delete(key(sessionId));
        } catch (Exception exception) {
            // Best-effort cache eviction.
        }
    }

    private String key(String sessionId) {
        return "chat:context:" + sessionId;
    }
}
