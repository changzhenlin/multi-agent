package com.company.chat.web.document;

import com.company.chat.agent.rag.chunk.ChunkingOptions;
import com.company.chat.agent.rag.chunk.ChunkingService;
import com.company.chat.agent.rag.chunk.DefaultChunkingService;
import com.company.chat.agent.rag.document.DefaultDocumentLoader;
import com.company.chat.agent.rag.document.DocumentLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DocumentIngestionConfiguration {

    @Bean
    DocumentLoader documentLoader() {
        return new DefaultDocumentLoader();
    }

    @Bean
    ChunkingService chunkingService() {
        return new DefaultChunkingService(new ChunkingOptions(640, 128));
    }
}
