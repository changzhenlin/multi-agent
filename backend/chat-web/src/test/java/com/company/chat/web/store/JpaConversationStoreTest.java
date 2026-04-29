package com.company.chat.web.store;

import com.company.chat.api.agent.AgentType;
import com.company.chat.api.message.Message;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.Pageable;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JpaConversationStoreTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-04-29T03:00:00Z"), ZoneOffset.UTC);

    @Test
    void createsSessionAndPersistsMessages() {
        ChatSessionRepository sessionRepository = mock(ChatSessionRepository.class);
        ChatMessageRepository messageRepository = mock(ChatMessageRepository.class);
        ObjectProvider<ConversationHistoryCache> historyCache = mock();
        ChatSessionEntity session = new ChatSessionEntity("session-1", "user-1", "测试", OffsetDateTime.now(CLOCK));
        when(historyCache.getIfAvailable()).thenReturn(null);
        when(sessionRepository.save(any(ChatSessionEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(sessionRepository.findById("session-1")).thenReturn(Optional.of(session));
        var store = new JpaConversationStore(sessionRepository, messageRepository, historyCache, CLOCK);

        store.ensureSession("session-1", "user-1", "测试");
        store.appendMessage("session-1", "assistant", "你好", AgentType.SIMPLE_CHAT);

        verify(sessionRepository, times(2)).save(any(ChatSessionEntity.class));
        verify(messageRepository).save(any(ChatMessageEntity.class));
        verify(sessionRepository).findById("session-1");
    }

    @Test
    void loadsRecentHistoryFromCacheBeforeDatabase() {
        ChatSessionRepository sessionRepository = mock(ChatSessionRepository.class);
        ChatMessageRepository messageRepository = mock(ChatMessageRepository.class);
        ObjectProvider<ConversationHistoryCache> historyCacheProvider = mock();
        ConversationHistoryCache historyCache = mock(ConversationHistoryCache.class);
        when(historyCacheProvider.getIfAvailable()).thenReturn(historyCache);
        when(historyCache.read("session-1")).thenReturn(Optional.of(List.of(new Message("user", "缓存消息"))));
        var store = new JpaConversationStore(sessionRepository, messageRepository, historyCacheProvider, CLOCK);

        List<Message> history = store.findRecentHistory("session-1", 20);

        assertThat(history).containsExactly(new Message("user", "缓存消息"));
    }

    @Test
    void loadsRecentHistoryFromDatabaseOnCacheMiss() {
        ChatSessionRepository sessionRepository = mock(ChatSessionRepository.class);
        ChatMessageRepository messageRepository = mock(ChatMessageRepository.class);
        ObjectProvider<ConversationHistoryCache> historyCacheProvider = mock();
        ConversationHistoryCache historyCache = mock(ConversationHistoryCache.class);
        OffsetDateTime now = OffsetDateTime.now(CLOCK);
        when(historyCacheProvider.getIfAvailable()).thenReturn(historyCache);
        when(historyCache.read("session-1")).thenReturn(Optional.empty());
        when(messageRepository.findBySessionIdOrderByCreatedAtDesc(eq("session-1"), any(Pageable.class)))
                .thenReturn(List.of(
                        new ChatMessageEntity("session-1", "assistant", "第二条", AgentType.SIMPLE_CHAT, now.plusSeconds(1)),
                        new ChatMessageEntity("session-1", "user", "第一条", null, now)));
        when(sessionRepository.findById("session-1"))
                .thenReturn(Optional.of(new ChatSessionEntity("session-1", "user-1", "测试", now)));
        var store = new JpaConversationStore(sessionRepository, messageRepository, historyCacheProvider, CLOCK);

        List<Message> history = store.findRecentHistory("session-1", 20);

        assertThat(history).containsExactly(
                new Message("user", "第一条"),
                new Message("assistant", "第二条"));
        verify(historyCache).write("session-1", "user-1", history);
    }
}
