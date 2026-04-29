package com.company.chat.web.store;

import com.company.chat.api.agent.AgentType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "chat_message")
class ChatMessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long messageId;

    @Column(name = "session_id", nullable = false, length = 64)
    private String sessionId;

    @Column(name = "role", nullable = false, length = 32)
    private String role;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "agent_type", length = 32)
    private AgentType agentType;

    @Column(name = "created_at", nullable = false, columnDefinition = "DATETIME(3)")
    private OffsetDateTime createdAt;

    protected ChatMessageEntity() {
    }

    ChatMessageEntity(String sessionId, String role, String content, AgentType agentType, OffsetDateTime createdAt) {
        this.sessionId = sessionId;
        this.role = role;
        this.content = content;
        this.agentType = agentType;
        this.createdAt = createdAt;
    }

    Long getMessageId() {
        return messageId;
    }

    String getSessionId() {
        return sessionId;
    }

    String getRole() {
        return role;
    }

    String getContent() {
        return content;
    }

    AgentType getAgentType() {
        return agentType;
    }

    OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
