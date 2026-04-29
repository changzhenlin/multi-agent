package com.company.chat.web.store;

import com.company.chat.api.agent.AgentType;
import com.company.chat.api.message.Message;
import com.company.chat.web.dto.ChatMessageResponse;
import com.company.chat.web.dto.SessionSummaryResponse;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class JpaConversationStore implements ConversationStore {

    private static final String DEFAULT_TITLE = "New chat";

    private final ChatSessionRepository sessionRepository;
    private final ChatMessageRepository messageRepository;
    private final ConversationHistoryCache historyCache;
    private final Clock clock;

    public JpaConversationStore(
            ChatSessionRepository sessionRepository,
            ChatMessageRepository messageRepository,
            ObjectProvider<ConversationHistoryCache> historyCache,
            Clock clock) {
        this.sessionRepository = Objects.requireNonNull(sessionRepository, "sessionRepository must not be null");
        this.messageRepository = Objects.requireNonNull(messageRepository, "messageRepository must not be null");
        this.historyCache = historyCache.getIfAvailable();
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    @Override
    @Transactional
    public void ensureSession(String sessionId, String userId, String title) {
        if (sessionRepository.existsById(sessionId)) {
            return;
        }
        sessionRepository.save(new ChatSessionEntity(sessionId, userId, normalizeTitle(title), now()));
    }

    @Override
    @Transactional
    public SessionSummaryResponse createSession(String userId, String title) {
        String sessionId = UUID.randomUUID().toString();
        var entity = sessionRepository.save(new ChatSessionEntity(sessionId, userId, normalizeTitle(title), now()));
        return toSummary(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionSummaryResponse> listSessions() {
        return sessionRepository.findAllByOrderByUpdatedAtDesc().stream()
                .map(this::toSummary)
                .toList();
    }

    @Override
    @Transactional
    public void deleteSession(String sessionId) {
        sessionRepository.deleteById(sessionId);
        if (historyCache != null) {
            historyCache.evict(sessionId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Message> findRecentHistory(String sessionId, int limit) {
        if (limit <= 0) {
            return List.of();
        }
        if (historyCache != null) {
            var cached = historyCache.read(sessionId);
            if (cached.isPresent()) {
                return cached.get();
            }
        }
        List<Message> history = messageRepository.findBySessionIdOrderByCreatedAtDesc(sessionId, PageRequest.of(0, limit)).stream()
                .sorted(Comparator.comparing(ChatMessageEntity::getCreatedAt))
                .map(entity -> new Message(entity.getRole(), entity.getContent()))
                .toList();
        if (historyCache != null) {
            sessionRepository.findById(sessionId)
                    .ifPresent(session -> historyCache.write(sessionId, session.getUserId(), history));
        }
        return history;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageResponse> listMessages(String sessionId) {
        return messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId).stream()
                .map(this::toMessageResponse)
                .toList();
    }

    @Override
    @Transactional
    public void appendMessage(String sessionId, String role, String content, AgentType agentType) {
        messageRepository.save(new ChatMessageEntity(sessionId, role, content, agentType, now()));
        sessionRepository.findById(sessionId).ifPresent(session -> {
            session.touch(now());
            sessionRepository.save(session);
            if (historyCache != null) {
                historyCache.evict(sessionId);
            }
        });
    }

    private SessionSummaryResponse toSummary(ChatSessionEntity entity) {
        return new SessionSummaryResponse(
                entity.getSessionId(),
                entity.getUserId(),
                entity.getTitle(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    private ChatMessageResponse toMessageResponse(ChatMessageEntity entity) {
        return new ChatMessageResponse(
                String.valueOf(entity.getMessageId()),
                entity.getSessionId(),
                entity.getRole(),
                entity.getContent(),
                entity.getAgentType(),
                entity.getCreatedAt());
    }

    private OffsetDateTime now() {
        return OffsetDateTime.now(clock);
    }

    private String normalizeTitle(String title) {
        if (title == null || title.isBlank()) {
            return DEFAULT_TITLE;
        }
        return title.strip();
    }
}
