package com.company.chat.agent.rag;

import com.company.chat.api.agent.ChatAgent;
import com.company.chat.api.agent.ChatContext;
import java.util.stream.Stream;

public class RagAgent implements ChatAgent {

    @Override
    public Stream<String> chat(ChatContext context, String userMessage) {
        throw new UnsupportedOperationException("TODO: implement RagAgent");
    }
}
