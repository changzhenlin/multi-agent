package com.company.chat.agent.rag.document;

public record LoadedDocument(
        String documentId,
        String fileName,
        String title,
        String sourceUri,
        String content) {
}
