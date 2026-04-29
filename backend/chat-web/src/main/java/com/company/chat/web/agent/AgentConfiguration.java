package com.company.chat.web.agent;

import com.company.chat.agent.rag.RagAgent;
import com.company.chat.agent.simple.SimpleChatAgent;
import com.company.chat.agent.sql.ServiceSqlAgent;
import com.company.chat.api.agent.AgentType;
import com.company.chat.api.agent.ChatAgent;
import com.company.chat.api.llm.LlmClient;
import com.company.chat.api.router.AgentRouter;
import com.company.chat.web.router.DefaultAgentRouter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class AgentConfiguration {

    @Bean
    public ChatAgent simpleChatAgent(LlmClient llmClient) {
        return new SimpleChatAgent(llmClient, SimpleChatAgent.DEFAULT_SYSTEM_PROMPT);
    }

    @Bean
    public ChatAgent ragAgent() {
        return new RagAgent();
    }

    @Bean
    public ChatAgent serviceSqlAgent() {
        return new ServiceSqlAgent();
    }

    @Bean
    public AgentRouter agentRouter(ChatAgent simpleChatAgent, ChatAgent ragAgent, ChatAgent serviceSqlAgent) {
        return new DefaultAgentRouter(Map.of(
                AgentType.SIMPLE_CHAT, simpleChatAgent,
                AgentType.RAG, ragAgent,
                AgentType.SERVICE_SQL, serviceSqlAgent));
    }
}
