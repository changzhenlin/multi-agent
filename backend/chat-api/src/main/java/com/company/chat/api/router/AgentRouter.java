package com.company.chat.api.router;

import com.company.chat.api.agent.AgentType;
import com.company.chat.api.agent.ChatAgent;

public interface AgentRouter {

    /**
     * 根据意图类型获取对应 Agent 实例
     *
     * @param agentType 目标 Agent 类型
     * @return 对应的 ChatAgent 实现
     */
    ChatAgent route(AgentType agentType);
}
