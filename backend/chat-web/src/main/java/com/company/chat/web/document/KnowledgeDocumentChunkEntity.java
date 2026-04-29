package com.company.chat.web.document;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "kb_document_chunk")
class KnowledgeDocumentChunkEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chunk_id")
    private Long chunkId;

    @Column(name = "doc_id", nullable = false, length = 64)
    private String documentId;

    @Column(name = "chunk_index", nullable = false)
    private int chunkIndex;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "source_uri", length = 1024)
    private String sourceUri;

    @Column(name = "start_offset", nullable = false)
    private int startOffset;

    @Column(name = "end_offset", nullable = false)
    private int endOffset;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    protected KnowledgeDocumentChunkEntity() {
    }

    KnowledgeDocumentChunkEntity(
            String documentId,
            int chunkIndex,
            String title,
            String sourceUri,
            int startOffset,
            int endOffset,
            String content) {
        this.documentId = documentId;
        this.chunkIndex = chunkIndex;
        this.title = title;
        this.sourceUri = sourceUri;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.content = content;
    }
}
