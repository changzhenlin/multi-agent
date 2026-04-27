package com.company.chat.agent.simple;

import com.company.chat.api.agent.ChatAgent;
import com.company.chat.api.agent.ChatContext;
import com.company.chat.api.llm.LlmClient;
import com.company.chat.api.message.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class SimpleChatAgent implements ChatAgent {

    public static final String DEFAULT_SYSTEM_PROMPT = "你是一个企业内部助手，请用准确、简洁、专业的方式回答员工问题。";

    private final LlmClient llmClient;
    private final String systemPrompt;

    public SimpleChatAgent(LlmClient llmClient, String systemPrompt) {
        this.llmClient = Objects.requireNonNull(llmClient, "llmClient must not be null");
        this.systemPrompt = normalizeSystemPrompt(systemPrompt);
    }

    @Override
    public Stream<String> chat(ChatContext context, String userMessage) {
        return llmClient.streamChat(buildMessages(context, userMessage)).toStream();
    }

    private List<Message> buildMessages(ChatContext context, String userMessage) {
        List<Message> messages = new ArrayList<>();
        messages.add(new Message("system", systemPrompt));

        if (context != null && context.history() != null) {
            messages.addAll(context.history());
        }

        messages.add(new Message("user", userMessage));
        return List.copyOf(messages);
    }

    private String normalizeSystemPrompt(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            return DEFAULT_SYSTEM_PROMPT;
        }
        return prompt;
    }
}
