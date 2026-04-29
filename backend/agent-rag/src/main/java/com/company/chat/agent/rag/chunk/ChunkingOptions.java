package com.company.chat.agent.rag.chunk;

public record ChunkingOptions(int maxChunkChars, int overlapChars) {

    public ChunkingOptions {
        if (maxChunkChars <= 0) {
            throw new IllegalArgumentException("maxChunkChars must be positive");
        }
        if (overlapChars < 0 || overlapChars >= maxChunkChars) {
            throw new IllegalArgumentException("overlapChars must be non-negative and smaller than maxChunkChars");
        }
    }
}
