package com.company.chat.web.orchestrator;

import com.company.chat.api.intent.IntentRecognizer;
import com.company.chat.api.router.AgentRouter;
import com.company.chat.web.store.ConversationStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrchestratorConfiguration {

    @Bean
    public ChatOrchestrator chatOrchestrator(
            IntentRecognizer intentRecognizer,
            AgentRouter agentRouter,
            ConversationStore conversationStore) {
        return new ChatOrchestrator(intentRecognizer, agentRouter, conversationStore);
    }
}
