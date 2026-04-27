package com.company.chat.api.llm;

import java.util.List;

public interface EmbeddingClient {

    /**
     * 批量文本向量化
     *
     * @param texts 文本列表
     * @return 向量列表（Kimi 为 1024 维）
     */
    List<float[]> embed(List<String> texts);
}
