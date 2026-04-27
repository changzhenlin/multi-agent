package com.company.chat.agent.sql;

import com.company.chat.api.agent.ChatAgent;
import com.company.chat.api.agent.ChatContext;
import java.util.stream.Stream;

public class ServiceSqlAgent implements ChatAgent {

    @Override
    public Stream<String> chat(ChatContext context, String userMessage) {
        throw new UnsupportedOperationException("TODO: implement ServiceSqlAgent");
    }
}
