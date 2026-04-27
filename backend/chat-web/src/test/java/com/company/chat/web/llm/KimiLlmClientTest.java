package com.company.chat.web.llm;

import com.company.chat.api.message.Message;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KimiLlmClientTest {

    private MockWebServer server;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    void streamChatParsesMoonshotSseContentChunks() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, "text/event-stream")
                .setBody("""
                        data: {"choices":[{"delta":{"content":"你"}}]}

                        data: {"choices":[{"delta":{"content":"好"}}]}

                        data: [DONE]

                        """));

        LlmProperties properties = new LlmProperties();
        properties.setBaseUrl(server.url("/v1").toString());
        properties.setApiKey("sk-test-secret");
        properties.setModel("moonshot-v1-8k");

        KimiLlmClient client = new KimiLlmClient(properties);

        List<String> chunks = client.streamChat(List.of(new Message("user", "你好")))
                .collectList()
                .block();

        assertThat(chunks).containsExactly("你", "好");

        var request = server.takeRequest();
        assertThat(request.getPath()).isEqualTo("/v1/chat/completions");
        assertThat(request.getHeader(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer sk-test-secret");
        assertThat(request.getBody().readUtf8())
                .contains("\"model\":\"moonshot-v1-8k\"")
                .contains("\"role\":\"user\"")
                .contains("\"content\":\"你好\"")
                .contains("\"stream\":true");
    }
}
