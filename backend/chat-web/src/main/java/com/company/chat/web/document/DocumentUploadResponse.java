package com.company.chat.web.document;

public record DocumentUploadResponse(
        String documentId,
        String title,
        int totalChunks) {
}
