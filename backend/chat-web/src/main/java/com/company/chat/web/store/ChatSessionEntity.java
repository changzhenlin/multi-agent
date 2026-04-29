package com.company.chat.web.store;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "chat_session")
class ChatSessionEntity {

    @Id
    @Column(name = "session_id", nullable = false, length = 64)
    private String sessionId;

    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "created_at", nullable = false, columnDefinition = "DATETIME(3)")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false, columnDefinition = "DATETIME(3)")
    private OffsetDateTime updatedAt;

    protected ChatSessionEntity() {
    }

    ChatSessionEntity(String sessionId, String userId, String title, OffsetDateTime now) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.title = title;
        this.createdAt = now;
        this.updatedAt = now;
    }

    String getSessionId() {
        return sessionId;
    }

    String getUserId() {
        return userId;
    }

    String getTitle() {
        return title;
    }

    OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    void touch(OffsetDateTime now) {
        this.updatedAt = now;
    }
}
