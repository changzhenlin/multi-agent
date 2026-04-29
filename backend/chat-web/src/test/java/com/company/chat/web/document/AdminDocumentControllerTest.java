package com.company.chat.web.document;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class AdminDocumentControllerTest {

    @Test
    void uploadsDocumentAndReturnsChunkSummary() {
        var service = new StubKnowledgeDocumentService();
        var controller = new AdminDocumentController(service);
        var file = new MockMultipartFile(
                "file",
                "handbook.md",
                "text/markdown",
                "# 员工手册\n\n请假流程需要审批。".getBytes(StandardCharsets.UTF_8));

        DocumentUploadResponse response = controller.uploadDocument(file, "/docs/handbook.md");

        assertThat(response.documentId()).isEqualTo("doc-1");
        assertThat(response.title()).isEqualTo("员工手册");
        assertThat(response.totalChunks()).isEqualTo(2);
        assertThat(service.fileName).isEqualTo("handbook.md");
        assertThat(service.sourceUri).isEqualTo("/docs/handbook.md");
    }

    private static class StubKnowledgeDocumentService implements KnowledgeDocumentService {
        private String fileName;
        private String sourceUri;

        @Override
        public DocumentUploadResponse ingest(String fileName, String sourceUri, byte[] content) {
            this.fileName = fileName;
            this.sourceUri = sourceUri;
            return new DocumentUploadResponse("doc-1", "员工手册", 2);
        }
    }
}
