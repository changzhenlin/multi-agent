package com.company.chat.web.document;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "kb_document")
class KnowledgeDocumentEntity {

    @Id
    @Column(name = "doc_id", nullable = false, length = 64)
    private String documentId;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "source_uri", length = 1024)
    private String sourceUri;

    @Column(name = "content", nullable = false, columnDefinition = "LONGTEXT")
    private String content;

    @Column(name = "total_chunks", nullable = false)
    private int totalChunks;

    @Column(name = "created_at", nullable = false, columnDefinition = "DATETIME(3)")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false, columnDefinition = "DATETIME(3)")
    private OffsetDateTime updatedAt;

    protected KnowledgeDocumentEntity() {
    }

    KnowledgeDocumentEntity(
            String documentId,
            String fileName,
            String title,
            String sourceUri,
            String content,
            int totalChunks,
            OffsetDateTime now) {
        this.documentId = documentId;
        this.fileName = fileName;
        this.title = title;
        this.sourceUri = sourceUri;
        this.content = content;
        this.totalChunks = totalChunks;
        this.createdAt = now;
        this.updatedAt = now;
    }

    String getDocumentId() {
        return documentId;
    }

    String getTitle() {
        return title;
    }

    int getTotalChunks() {
        return totalChunks;
    }
}
