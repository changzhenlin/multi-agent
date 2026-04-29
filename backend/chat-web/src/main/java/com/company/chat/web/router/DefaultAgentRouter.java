package com.company.chat.web.router;

import com.company.chat.api.agent.AgentType;
import com.company.chat.api.agent.ChatAgent;
import com.company.chat.api.router.AgentRouter;

import java.util.Map;

public class DefaultAgentRouter implements AgentRouter {

    private final Map<AgentType, ChatAgent> agents;

    public DefaultAgentRouter(Map<AgentType, ChatAgent> agents) {
        this.agents = Map.copyOf(agents);
    }

    @Override
    public ChatAgent route(AgentType agentType) {
        ChatAgent agent = agents.get(agentType);
        if (agent == null) {
            throw new IllegalArgumentException("No ChatAgent registered for " + agentType);
        }
        return agent;
    }
}
