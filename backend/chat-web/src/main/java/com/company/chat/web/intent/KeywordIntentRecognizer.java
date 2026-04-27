package com.company.chat.web.intent;

import com.company.chat.api.agent.AgentType;
import com.company.chat.api.intent.IntentRecognizer;

public class KeywordIntentRecognizer implements IntentRecognizer {

    private final IntentProperties properties;

    public KeywordIntentRecognizer(IntentProperties properties) {
        this.properties = properties;
    }

    @Override
    public AgentType recognize(String userMessage) {
        String message = userMessage == null ? "" : userMessage;

        if (matchesAny(message, properties.getSqlKeywords())) {
            return AgentType.SERVICE_SQL;
        }
        if (matchesAny(message, properties.getRagKeywords())) {
            return AgentType.RAG;
        }
        return AgentType.SIMPLE_CHAT;
    }

    private boolean matchesAny(String message, Iterable<String> keywords) {
        for (String keyword : keywords) {
            if (message.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
