package com.company.chat.web.dto;

import java.time.OffsetDateTime;

public record SessionSummaryResponse(
        String id,
        String userId,
        String title,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
