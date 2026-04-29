package com.company.chat.agent.rag.chunk;

public record DocumentChunk(
        String documentId,
        String title,
        String sourceUri,
        int chunkIndex,
        int startOffset,
        int endOffset,
        String content) {
}
