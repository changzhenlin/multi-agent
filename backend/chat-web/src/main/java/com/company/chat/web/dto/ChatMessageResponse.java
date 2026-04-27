package com.company.chat.web.dto;

import com.company.chat.api.agent.AgentType;

import java.time.OffsetDateTime;

public record ChatMessageResponse(
        String id,
        String sessionId,
        String role,
        String content,
        AgentType agentType,
        OffsetDateTime createdAt
) {
}
