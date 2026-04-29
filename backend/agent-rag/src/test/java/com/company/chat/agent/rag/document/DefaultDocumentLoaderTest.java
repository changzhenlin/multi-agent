package com.company.chat.agent.rag.document;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefaultDocumentLoaderTest {

    private final DefaultDocumentLoader loader = new DefaultDocumentLoader();

    @Test
    void loadsMarkdownTextAndTitleFromFirstHeading() {
        LoadedDocument document = loader.load("handbook.md", "/docs/handbook.md",
                "# 员工手册\n\n请假流程需要主管审批。".getBytes(StandardCharsets.UTF_8));

        assertThat(document.documentId()).isNotBlank();
        assertThat(document.fileName()).isEqualTo("handbook.md");
        assertThat(document.title()).isEqualTo("员工手册");
        assertThat(document.sourceUri()).isEqualTo("/docs/handbook.md");
        assertThat(document.content()).contains("请假流程需要主管审批。");
    }

    @Test
    void loadsTxtWithFileNameAsFallbackTitle() {
        LoadedDocument document = loader.load("policy.txt", null,
                "报销制度按月结算。".getBytes(StandardCharsets.UTF_8));

        assertThat(document.title()).isEqualTo("policy");
        assertThat(document.content()).isEqualTo("报销制度按月结算。");
    }

    @Test
    void rejectsUnsupportedFileType() {
        assertThatThrownBy(() -> loader.load("image.png", null, new byte[] {1, 2, 3}))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported document type");
    }
}
