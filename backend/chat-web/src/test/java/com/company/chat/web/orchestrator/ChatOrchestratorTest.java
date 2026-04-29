package com.company.chat.web.orchestrator;

import com.company.chat.api.agent.AgentType;
import com.company.chat.api.agent.ChatAgent;
import com.company.chat.api.agent.ChatContext;
import com.company.chat.api.intent.IntentRecognizer;
import com.company.chat.api.message.Message;
import com.company.chat.api.router.AgentRouter;
import com.company.chat.web.dto.ChatMessageResponse;
import com.company.chat.web.dto.SessionSummaryResponse;
import com.company.chat.web.store.ConversationStore;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ChatOrchestratorTest {

    @Test
    void recognizesIntentRoutesAgentAndStreamsResponse() {
        CapturingAgent agent = new CapturingAgent();
        IntentRecognizer recognizer = message -> AgentType.SIMPLE_CHAT;
        AgentRouter router = agentType -> {
            assertThat(agentType).isEqualTo(AgentType.SIMPLE_CHAT);
            return agent;
        };
        var store = new CapturingConversationStore(List.of(new Message("user", "上一轮")));
        ChatOrchestrator orchestrator = new ChatOrchestrator(recognizer, router, store);

        List<String> chunks;
        try (var stream = orchestrator.chat("session-1", "user-1", "你好")) {
            chunks = stream.toList();
        }

        assertThat(chunks).containsExactly("你", "好");
        assertThat(agent.context.sessionId()).isEqualTo("session-1");
        assertThat(agent.context.userId()).isEqualTo("user-1");
        assertThat(agent.context.history()).containsExactly(new Message("user", "上一轮"));
        assertThat(agent.userMessage).isEqualTo("你好");
        assertThat(store.ensureSessionIds).containsExactly("session-1");
        assertThat(store.savedMessages)
                .extracting(SavedMessage::role, SavedMessage::content, SavedMessage::agentType)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("user", "你好", null),
                        org.assertj.core.groups.Tuple.tuple("assistant", "你好", AgentType.SIMPLE_CHAT));
    }

    private static class CapturingAgent implements ChatAgent {
        private ChatContext context;
        private String userMessage;

        @Override
        public Stream<String> chat(ChatContext context, String userMessage) {
            this.context = context;
            this.userMessage = userMessage;
            return Stream.of("你", "好");
        }
    }

    private record SavedMessage(String role, String content, AgentType agentType) {
    }

    private static class CapturingConversationStore implements ConversationStore {
        private final List<Message> history;
        private final List<String> ensureSessionIds = new ArrayList<>();
        private final List<SavedMessage> savedMessages = new ArrayList<>();

        CapturingConversationStore(List<Message> history) {
            this.history = history;
        }

        @Override
        public void ensureSession(String sessionId, String userId, String title) {
            ensureSessionIds.add(sessionId);
        }

        @Override
        public SessionSummaryResponse createSession(String userId, String title) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<SessionSummaryResponse> listSessions() {
            return List.of();
        }

        @Override
        public void deleteSession(String sessionId) {
        }

        @Override
        public List<Message> findRecentHistory(String sessionId, int limit) {
            return history;
        }

        @Override
        public List<ChatMessageResponse> listMessages(String sessionId) {
            return List.of();
        }

        @Override
        public void appendMessage(String sessionId, String role, String content, AgentType agentType) {
            savedMessages.add(new SavedMessage(role, content, agentType));
        }
    }
}
