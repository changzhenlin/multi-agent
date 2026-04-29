package com.company.chat.web.document;

import com.company.chat.agent.rag.chunk.ChunkingService;
import com.company.chat.agent.rag.chunk.DocumentChunk;
import com.company.chat.agent.rag.document.DocumentLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Objects;

@Service
public class JpaKnowledgeDocumentService implements KnowledgeDocumentService {

    private final DocumentLoader documentLoader;
    private final ChunkingService chunkingService;
    private final KnowledgeDocumentRepository documentRepository;
    private final KnowledgeDocumentChunkRepository chunkRepository;

    public JpaKnowledgeDocumentService(
            DocumentLoader documentLoader,
            ChunkingService chunkingService,
            KnowledgeDocumentRepository documentRepository,
            KnowledgeDocumentChunkRepository chunkRepository) {
        this.documentLoader = Objects.requireNonNull(documentLoader, "documentLoader must not be null");
        this.chunkingService = Objects.requireNonNull(chunkingService, "chunkingService must not be null");
        this.documentRepository = Objects.requireNonNull(documentRepository, "documentRepository must not be null");
        this.chunkRepository = Objects.requireNonNull(chunkRepository, "chunkRepository must not be null");
    }

    @Override
    @Transactional
    public DocumentUploadResponse ingest(String fileName, String sourceUri, byte[] content) {
        var document = documentLoader.load(fileName, sourceUri, content);
        var chunks = chunkingService.chunk(document);
        var entity = documentRepository.save(new KnowledgeDocumentEntity(
                document.documentId(),
                document.fileName(),
                document.title(),
                document.sourceUri(),
                document.content(),
                chunks.size(),
                OffsetDateTime.now()));
        chunkRepository.saveAll(chunks.stream()
                .map(this::toEntity)
                .toList());
        return new DocumentUploadResponse(entity.getDocumentId(), entity.getTitle(), entity.getTotalChunks());
    }

    private KnowledgeDocumentChunkEntity toEntity(DocumentChunk chunk) {
        return new KnowledgeDocumentChunkEntity(
                chunk.documentId(),
                chunk.chunkIndex(),
                chunk.title(),
                chunk.sourceUri(),
                chunk.startOffset(),
                chunk.endOffset(),
                chunk.content());
    }
}
