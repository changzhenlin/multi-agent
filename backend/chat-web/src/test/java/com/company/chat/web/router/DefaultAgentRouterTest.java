package com.company.chat.web.router;

import com.company.chat.api.agent.AgentType;
import com.company.chat.api.agent.ChatAgent;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefaultAgentRouterTest {

    @Test
    void routesToAgentByType() {
        ChatAgent simple = (context, message) -> java.util.stream.Stream.of("simple");
        ChatAgent rag = (context, message) -> java.util.stream.Stream.of("rag");
        ChatAgent sql = (context, message) -> java.util.stream.Stream.of("sql");
        DefaultAgentRouter router = new DefaultAgentRouter(Map.of(
                AgentType.SIMPLE_CHAT, simple,
                AgentType.RAG, rag,
                AgentType.SERVICE_SQL, sql));

        assertThat(router.route(AgentType.SIMPLE_CHAT)).isSameAs(simple);
        assertThat(router.route(AgentType.RAG)).isSameAs(rag);
        assertThat(router.route(AgentType.SERVICE_SQL)).isSameAs(sql);
    }

    @Test
    void rejectsMissingAgentMapping() {
        DefaultAgentRouter router = new DefaultAgentRouter(Map.of());

        assertThatThrownBy(() -> router.route(AgentType.SIMPLE_CHAT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SIMPLE_CHAT");
    }
}
