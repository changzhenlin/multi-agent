package com.company.chat.web.dto;

public record ChatMessageRequest(
        String sessionId,
        String userId,
        String message
) {
}
