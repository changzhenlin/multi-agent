package com.company.chat.agent.rag.document;

public interface DocumentLoader {

    LoadedDocument load(String fileName, String sourceUri, byte[] content);
}
