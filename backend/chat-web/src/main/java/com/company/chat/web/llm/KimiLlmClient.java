package com.company.chat.web.llm;

import com.company.chat.api.llm.LlmClient;
import com.company.chat.api.message.Message;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class KimiLlmClient implements LlmClient {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final LlmProperties properties;
    private final WebClient webClient;

    public KimiLlmClient(LlmProperties properties) {
        this.properties = properties;
        this.webClient = WebClient.builder()
                .baseUrl(properties.getBaseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getApiKey())
                .build();
    }

    @Override
    public Flux<String> streamChat(List<Message> messages) {
        return webClient.post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .bodyValue(requestBody(messages))
                .retrieve()
                .bodyToFlux(String.class)
                .flatMapIterable(this::extractContentChunks);
    }

    private Map<String, Object> requestBody(List<Message> messages) {
        List<Map<String, String>> requestMessages = messages.stream()
                .map(message -> Map.of(
                        "role", message.role(),
                        "content", message.content()))
                .toList();

        return Map.of(
                "model", properties.getModel(),
                "messages", requestMessages,
                "stream", true);
    }

    private List<String> extractContentChunks(String rawChunk) {
        List<String> chunks = new ArrayList<>();
        for (String line : rawChunk.split("\\R")) {
            String payload = line.strip();
            if (payload.isBlank()) {
                continue;
            }
            if (payload.startsWith("data:")) {
                payload = payload.substring("data:".length()).strip();
            }
            if ("[DONE]".equals(payload)) {
                continue;
            }
            extractContent(payload, chunks);
        }
        return chunks;
    }

    private void extractContent(String payload, List<String> chunks) {
        try {
            JsonNode root = OBJECT_MAPPER.readTree(payload);
            JsonNode content = root.path("choices").path(0).path("delta").path("content");
            if (content.isTextual() && !content.asText().isEmpty()) {
                chunks.add(content.asText());
            }
        } catch (Exception ignored) {
            // Ignore malformed SSE control frames; the upstream stream can include comments or keep-alives.
        }
    }
}
