package com.company.chat.agent.simple;

import com.company.chat.api.agent.ChatContext;
import com.company.chat.api.llm.LlmClient;
import com.company.chat.api.message.Message;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SimpleChatAgentTest {

    @Test
    void sendsSystemPromptHistoryAndCurrentUserMessageToLlm() {
        CapturingLlmClient llmClient = new CapturingLlmClient();
        SimpleChatAgent agent = new SimpleChatAgent(llmClient, "你是企业内部助手");
        ChatContext context = new ChatContext("session-1", "user-1", List.of(
                new Message("user", "上一轮问题"),
                new Message("assistant", "上一轮回答")));

        List<String> chunks = agent.chat(context, "你好").toList();

        assertThat(chunks).containsExactly("你", "好");
        assertThat(llmClient.messages).containsExactly(
                new Message("system", "你是企业内部助手"),
                new Message("user", "上一轮问题"),
                new Message("assistant", "上一轮回答"),
                new Message("user", "你好"));
    }

    @Test
    void usesDefaultSystemPromptWhenPromptIsBlank() {
        CapturingLlmClient llmClient = new CapturingLlmClient();
        SimpleChatAgent agent = new SimpleChatAgent(llmClient, " ");
        ChatContext context = new ChatContext("session-1", "user-1", List.of());

        agent.chat(context, "你好").toList();

        assertThat(llmClient.messages.getFirst()).isEqualTo(new Message("system", SimpleChatAgent.DEFAULT_SYSTEM_PROMPT));
    }

    private static class CapturingLlmClient implements LlmClient {
        private List<Message> messages = new ArrayList<>();

        @Override
        public Flux<String> streamChat(List<Message> messages) {
            this.messages = List.copyOf(messages);
            return Flux.just("你", "好");
        }
    }
}
