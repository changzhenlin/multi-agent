package com.company.chat.web.document;

import com.company.chat.agent.rag.chunk.ChunkingOptions;
import com.company.chat.agent.rag.chunk.DefaultChunkingService;
import com.company.chat.agent.rag.document.DefaultDocumentLoader;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JpaKnowledgeDocumentServiceTest {

    @Test
    void loadsChunksAndPersistsDocumentSummary() {
        KnowledgeDocumentRepository documentRepository = mock(KnowledgeDocumentRepository.class);
        KnowledgeDocumentChunkRepository chunkRepository = mock(KnowledgeDocumentChunkRepository.class);
        when(documentRepository.save(any(KnowledgeDocumentEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var service = new JpaKnowledgeDocumentService(
                new DefaultDocumentLoader(),
                new DefaultChunkingService(new ChunkingOptions(24, 8)),
                documentRepository,
                chunkRepository);

        DocumentUploadResponse response = service.ingest(
                "handbook.md",
                "/docs/handbook.md",
                "# 员工手册\n\n第一句说明请假流程。第二句说明审批节点。第三句说明归档要求。".getBytes(StandardCharsets.UTF_8));

        assertThat(response.documentId()).isNotBlank();
        assertThat(response.title()).isEqualTo("员工手册");
        assertThat(response.totalChunks()).isGreaterThan(1);
        verify(documentRepository).save(any(KnowledgeDocumentEntity.class));
        verify(chunkRepository).saveAll(any());
    }
}
