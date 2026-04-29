package com.company.chat.agent.rag.chunk;

import com.company.chat.agent.rag.document.LoadedDocument;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultChunkingServiceTest {

    @Test
    void splitsLongChineseDocumentAtSentenceBoundaryWithOverlap() {
        LoadedDocument document = new LoadedDocument(
                "doc-1",
                "handbook.md",
                "员工手册",
                "/docs/handbook.md",
                "第一句说明请假流程。第二句说明审批节点。第三句说明归档要求。第四句说明补充材料。");
        DefaultChunkingService service = new DefaultChunkingService(new ChunkingOptions(24, 8));

        var chunks = service.chunk(document);

        assertThat(chunks).hasSizeGreaterThan(1);
        assertThat(chunks.get(0).content()).endsWith("。");
        assertThat(chunks.get(0).documentId()).isEqualTo("doc-1");
        assertThat(chunks.get(0).title()).isEqualTo("员工手册");
        assertThat(chunks.get(0).chunkIndex()).isZero();
        assertThat(chunks.get(1).content())
                .startsWith(chunks.get(0).content().substring(chunks.get(0).content().length() - 8));
    }

    @Test
    void returnsSingleChunkForShortDocument() {
        LoadedDocument document = new LoadedDocument(
                "doc-1",
                "faq.txt",
                "FAQ",
                null,
                "短文档无需拆分。");
        DefaultChunkingService service = new DefaultChunkingService(new ChunkingOptions(512, 128));

        var chunks = service.chunk(document);

        assertThat(chunks).singleElement().satisfies(chunk -> {
            assertThat(chunk.content()).isEqualTo("短文档无需拆分。");
            assertThat(chunk.startOffset()).isZero();
            assertThat(chunk.endOffset()).isEqualTo("短文档无需拆分。".length());
        });
    }
}
