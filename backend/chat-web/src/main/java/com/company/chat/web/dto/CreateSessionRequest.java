package com.company.chat.web.dto;

public record CreateSessionRequest(
        String userId,
        String title
) {
}
