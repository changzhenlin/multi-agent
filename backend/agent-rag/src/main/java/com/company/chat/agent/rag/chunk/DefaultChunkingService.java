package com.company.chat.agent.rag.chunk;

import com.company.chat.agent.rag.document.LoadedDocument;

import java.util.ArrayList;
import java.util.List;

public class DefaultChunkingService implements ChunkingService {

    private static final String SENTENCE_ENDINGS = "。！？.!?\n";

    private final ChunkingOptions options;

    public DefaultChunkingService(ChunkingOptions options) {
        this.options = options;
    }

    @Override
    public List<DocumentChunk> chunk(LoadedDocument document) {
        String content = document.content().strip();
        if (content.isEmpty()) {
            return List.of();
        }

        List<DocumentChunk> chunks = new ArrayList<>();
        int start = 0;
        while (start < content.length()) {
            int end = chooseEnd(content, start);
            String chunkContent = content.substring(start, end).strip();
            chunks.add(new DocumentChunk(
                    document.documentId(),
                    document.title(),
                    document.sourceUri(),
                    chunks.size(),
                    start,
                    end,
                    chunkContent));
            if (end >= content.length()) {
                break;
            }
            start = Math.max(0, end - options.overlapChars());
        }
        return chunks;
    }

    private int chooseEnd(String content, int start) {
        int hardEnd = Math.min(content.length(), start + options.maxChunkChars());
        if (hardEnd == content.length()) {
            return hardEnd;
        }
        for (int index = hardEnd - 1; index > start; index--) {
            if (SENTENCE_ENDINGS.indexOf(content.charAt(index)) >= 0) {
                return index + 1;
            }
        }
        return hardEnd;
    }
}
