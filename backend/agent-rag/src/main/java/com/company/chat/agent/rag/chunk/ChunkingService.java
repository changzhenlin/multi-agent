package com.company.chat.agent.rag.chunk;

import com.company.chat.agent.rag.document.LoadedDocument;

import java.util.List;

public interface ChunkingService {

    List<DocumentChunk> chunk(LoadedDocument document);
}
