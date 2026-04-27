package com.company.chat.agent.simple;

import com.company.chat.api.agent.ChatAgent;
import com.company.chat.api.agent.ChatContext;
import java.util.stream.Stream;

public class SimpleChatAgent implements ChatAgent {

    @Override
    public Stream<String> chat(ChatContext context, String userMessage) {
        throw new UnsupportedOperationException("TODO: implement SimpleChatAgent");
    }
}
