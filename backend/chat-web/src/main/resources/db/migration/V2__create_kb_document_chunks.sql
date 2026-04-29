CREATE TABLE kb_document_chunk (
    chunk_id BIGINT NOT NULL AUTO_INCREMENT,
    doc_id VARCHAR(64) NOT NULL COMMENT 'Knowledge document id',
    chunk_index INT NOT NULL COMMENT 'Zero-based chunk index within document',
    title VARCHAR(255) NOT NULL COMMENT 'Document title copied for retrieval metadata',
    source_uri VARCHAR(1024) NULL COMMENT 'Original source URI or internal link',
    start_offset INT NOT NULL COMMENT 'Start character offset in original content',
    end_offset INT NOT NULL COMMENT 'End character offset in original content',
    content TEXT NOT NULL COMMENT 'Chunk text',
    PRIMARY KEY (chunk_id),
    UNIQUE KEY uk_kb_document_chunk_doc_index (doc_id, chunk_index),
    KEY idx_kb_document_chunk_doc (doc_id),
    CONSTRAINT fk_kb_document_chunk_doc
        FOREIGN KEY (doc_id) REFERENCES kb_document (doc_id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Knowledge document chunks before embedding';
