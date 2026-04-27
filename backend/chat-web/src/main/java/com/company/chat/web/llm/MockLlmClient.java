package com.company.chat.web.llm;

import com.company.chat.api.llm.LlmClient;
import com.company.chat.api.message.Message;
import reactor.core.publisher.Flux;

import java.util.List;

public class MockLlmClient implements LlmClient {

    @Override
    public Flux<String> streamChat(List<Message> messages) {
        return Flux.just("Mock LLM response");
    }
}
