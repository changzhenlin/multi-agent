CREATE TABLE chat_session (
    session_id VARCHAR(64) NOT NULL COMMENT 'Session id',
    user_id VARCHAR(64) NOT NULL COMMENT 'User id from enterprise gateway',
    title VARCHAR(255) NOT NULL DEFAULT 'New chat' COMMENT 'Session title',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (session_id),
    KEY idx_chat_session_user_updated (user_id, updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Chat sessions';

CREATE TABLE chat_message (
    message_id BIGINT NOT NULL AUTO_INCREMENT,
    session_id VARCHAR(64) NOT NULL COMMENT 'Owning session id',
    role VARCHAR(32) NOT NULL COMMENT 'Message role: user, assistant, or system',
    content TEXT NOT NULL COMMENT 'Message body',
    agent_type VARCHAR(32) NULL COMMENT 'Agent that produced the assistant message',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (message_id),
    KEY idx_chat_message_session_created (session_id, created_at),
    CONSTRAINT fk_chat_message_session
        FOREIGN KEY (session_id) REFERENCES chat_session (session_id)
        ON DELETE CASCADE,
    CONSTRAINT chk_chat_message_role
        CHECK (role IN ('user', 'assistant', 'system')),
    CONSTRAINT chk_chat_message_agent_type
        CHECK (agent_type IS NULL OR agent_type IN ('SIMPLE_CHAT', 'RAG', 'SERVICE_SQL'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Chat messages';

CREATE TABLE kb_document (
    doc_id VARCHAR(64) NOT NULL COMMENT 'Knowledge document id',
    file_name VARCHAR(255) NOT NULL COMMENT 'Original file name',
    title VARCHAR(255) NOT NULL COMMENT 'Display title',
    source_uri VARCHAR(1024) NULL COMMENT 'Original source uri or internal link',
    content LONGTEXT NOT NULL COMMENT 'Original extracted document text',
    total_chunks INT NOT NULL DEFAULT 0 COMMENT 'Number of generated chunks',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (doc_id),
    KEY idx_kb_document_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Knowledge base documents';
