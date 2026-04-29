package com.company.chat.agent.rag.document;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.UUID;

public class DefaultDocumentLoader implements DocumentLoader {

    @Override
    public LoadedDocument load(String fileName, String sourceUri, byte[] content) {
        String extension = extension(fileName);
        if (!extension.equals("md") && !extension.equals("markdown") && !extension.equals("txt") && !extension.equals("pdf")) {
            throw new IllegalArgumentException("Unsupported document type: " + fileName);
        }

        String text = new String(content, StandardCharsets.UTF_8).strip();
        return new LoadedDocument(
                UUID.randomUUID().toString(),
                fileName,
                title(fileName, extension, text),
                sourceUri,
                text);
    }

    private String title(String fileName, String extension, String content) {
        if (extension.equals("md") || extension.equals("markdown")) {
            for (String line : content.split("\\R")) {
                String stripped = line.strip();
                if (stripped.startsWith("#")) {
                    return stripped.replaceFirst("^#+\\s*", "").strip();
                }
            }
        }
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;
    }

    private String extension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }
}
