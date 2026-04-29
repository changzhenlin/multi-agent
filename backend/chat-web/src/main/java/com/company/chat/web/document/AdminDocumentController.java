package com.company.chat.web.document;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
public class AdminDocumentController {

    private final KnowledgeDocumentService knowledgeDocumentService;

    public AdminDocumentController(KnowledgeDocumentService knowledgeDocumentService) {
        this.knowledgeDocumentService = knowledgeDocumentService;
    }

    @PostMapping(value = "/api/admin/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DocumentUploadResponse uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "sourceUri", required = false) String sourceUri) {
        try {
            return knowledgeDocumentService.ingest(file.getOriginalFilename(), sourceUri, file.getBytes());
        } catch (IOException exception) {
            throw new IllegalArgumentException("Failed to read uploaded document", exception);
        }
    }
}
