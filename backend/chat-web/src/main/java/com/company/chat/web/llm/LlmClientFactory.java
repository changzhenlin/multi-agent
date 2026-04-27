package com.company.chat.web.llm;

import com.company.chat.api.llm.LlmClient;

public class LlmClientFactory {

    private final LlmProperties properties;

    public LlmClientFactory(LlmProperties properties) {
        this.properties = properties;
    }

    public LlmClient create() {
        return switch (properties.getProvider()) {
            case KIMI -> new KimiLlmClient(properties);
            case MOCK, OPENAI, LOCAL -> new MockLlmClient();
        };
    }
}
