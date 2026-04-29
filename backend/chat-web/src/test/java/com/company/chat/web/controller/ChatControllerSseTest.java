package com.company.chat.web.controller;

import com.company.chat.api.agent.AgentType;
import com.company.chat.api.intent.IntentRecognizer;
import com.company.chat.api.message.Message;
import com.company.chat.api.router.AgentRouter;
import com.company.chat.web.dto.ChatMessageRequest;
import com.company.chat.web.dto.ChatMessageResponse;
import com.company.chat.web.dto.SessionSummaryResponse;
import com.company.chat.web.orchestrator.ChatOrchestrator;
import com.company.chat.web.store.ConversationStore;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.OffsetDateTime;
import java.util.stream.Stream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChatControllerSseTest {

    @Test
    void chatStreamReturnsSseEmitterForValidRequest() {
        IntentRecognizer recognizer = message -> AgentType.SIMPLE_CHAT;
        AgentRouter router = agentType -> (context, message) -> Stream.of("你", "好");
        var store = new InMemoryConversationStore();
        ChatController controller = new ChatController(new ChatOrchestrator(recognizer, router, store), store);

        SseEmitter emitter = controller.chatStream(new ChatMessageRequest("session-1", "user-1", "你好"));

        assertThat(emitter).isNotNull();
    }

    @Test
    void sessionEndpointsDelegateToConversationStore() {
        var store = new InMemoryConversationStore();
        ChatController controller = new ChatController(
                new ChatOrchestrator(message -> AgentType.SIMPLE_CHAT, agentType -> (context, message) -> Stream.of(), store),
                store);

        SessionSummaryResponse created = controller.createSession(new com.company.chat.web.dto.CreateSessionRequest("user-1", "测试"));

        assertThat(controller.listSessions()).containsExactly(created);
        assertThat(controller.listMessages(created.id())).isEmpty();

        controller.deleteSession(created.id());

        assertThat(controller.listSessions()).isEmpty();
    }

    private static class InMemoryConversationStore implements ConversationStore {
        private final java.util.Map<String, SessionSummaryResponse> sessions = new java.util.LinkedHashMap<>();

        @Override
        public void ensureSession(String sessionId, String userId, String title) {
            sessions.putIfAbsent(sessionId, new SessionSummaryResponse(sessionId, userId, title, OffsetDateTime.now(), OffsetDateTime.now()));
        }

        @Override
        public SessionSummaryResponse createSession(String userId, String title) {
            var session = new SessionSummaryResponse("session-1", userId, title, OffsetDateTime.now(), OffsetDateTime.now());
            sessions.put(session.id(), session);
            return session;
        }

        @Override
        public List<SessionSummaryResponse> listSessions() {
            return List.copyOf(sessions.values());
        }

        @Override
        public void deleteSession(String sessionId) {
            sessions.remove(sessionId);
        }

        @Override
        public List<Message> findRecentHistory(String sessionId, int limit) {
            return List.of();
        }

        @Override
        public List<ChatMessageResponse> listMessages(String sessionId) {
            return List.of();
        }

        @Override
        public void appendMessage(String sessionId, String role, String content, AgentType agentType) {
        }
    }
}
