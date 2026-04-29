package com.company.chat.web.orchestrator;

import com.company.chat.api.agent.ChatContext;
import com.company.chat.api.intent.IntentRecognizer;
import com.company.chat.api.router.AgentRouter;
import com.company.chat.web.store.ConversationStore;

import java.util.Objects;
import java.util.stream.Stream;

public class ChatOrchestrator {

    private static final int HISTORY_LIMIT = 20;

    private final IntentRecognizer intentRecognizer;
    private final AgentRouter agentRouter;
    private final ConversationStore conversationStore;

    public ChatOrchestrator(IntentRecognizer intentRecognizer, AgentRouter agentRouter, ConversationStore conversationStore) {
        this.intentRecognizer = Objects.requireNonNull(intentRecognizer, "intentRecognizer must not be null");
        this.agentRouter = Objects.requireNonNull(agentRouter, "agentRouter must not be null");
        this.conversationStore = Objects.requireNonNull(conversationStore, "conversationStore must not be null");
    }

    public Stream<String> chat(String sessionId, String userId, String userMessage) {
        var agentType = intentRecognizer.recognize(userMessage);
        var agent = agentRouter.route(agentType);
        conversationStore.ensureSession(sessionId, userId, defaultTitle(userMessage));
        var history = conversationStore.findRecentHistory(sessionId, HISTORY_LIMIT);
        conversationStore.appendMessage(sessionId, "user", userMessage, null);
        var context = new ChatContext(sessionId, userId, history);
        var assistantMessage = new StringBuilder();
        return agent.chat(context, userMessage)
                .peek(assistantMessage::append)
                .onClose(() -> saveAssistantMessage(sessionId, assistantMessage, agentType));
    }

    private void saveAssistantMessage(String sessionId, StringBuilder assistantMessage, com.company.chat.api.agent.AgentType agentType) {
        if (!assistantMessage.isEmpty()) {
            conversationStore.appendMessage(sessionId, "assistant", assistantMessage.toString(), agentType);
        }
    }

    private String defaultTitle(String userMessage) {
        if (userMessage == null || userMessage.isBlank()) {
            return "New chat";
        }
        String stripped = userMessage.strip();
        return stripped.length() <= 30 ? stripped : stripped.substring(0, 30);
    }
}
