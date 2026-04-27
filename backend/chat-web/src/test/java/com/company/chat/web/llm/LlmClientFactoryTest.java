package com.company.chat.web.llm;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LlmClientFactoryTest {

    @Test
    void createsMockClientWhenProviderIsMock() {
        LlmProperties properties = new LlmProperties();
        properties.setProvider(LlmProvider.MOCK);

        assertThat(new LlmClientFactory(properties).create()).isInstanceOf(MockLlmClient.class);
    }

    @Test
    void createsKimiClientWhenProviderIsKimi() {
        LlmProperties properties = new LlmProperties();
        properties.setProvider(LlmProvider.KIMI);
        properties.setApiKey("sk-test-secret");

        assertThat(new LlmClientFactory(properties).create()).isInstanceOf(KimiLlmClient.class);
    }

    @Test
    void masksApiKeyForDiagnosticOutput() {
        LlmProperties properties = new LlmProperties();
        properties.setApiKey("sk-1234567890");

        assertThat(properties.maskedApiKey())
                .doesNotContain("1234567890")
                .isEqualTo("sk-1********90");
    }
}
