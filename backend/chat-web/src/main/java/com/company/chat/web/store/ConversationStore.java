package com.company.chat.web.store;

import com.company.chat.api.agent.AgentType;
import com.company.chat.api.message.Message;
import com.company.chat.web.dto.ChatMessageResponse;
import com.company.chat.web.dto.SessionSummaryResponse;

import java.util.List;

public interface ConversationStore {

    void ensureSession(String sessionId, String userId, String title);

    SessionSummaryResponse createSession(String userId, String title);

    List<SessionSummaryResponse> listSessions();

    void deleteSession(String sessionId);

    List<Message> findRecentHistory(String sessionId, int limit);

    List<ChatMessageResponse> listMessages(String sessionId);

    void appendMessage(String sessionId, String role, String content, AgentType agentType);
}
