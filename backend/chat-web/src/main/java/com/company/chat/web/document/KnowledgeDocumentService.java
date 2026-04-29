package com.company.chat.web.document;

public interface KnowledgeDocumentService {

    DocumentUploadResponse ingest(String fileName, String sourceUri, byte[] content);
}
