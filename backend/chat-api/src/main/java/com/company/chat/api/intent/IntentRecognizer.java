package com.company.chat.api.intent;

import com.company.chat.api.agent.AgentType;

public interface IntentRecognizer {

    /**
     * 识别用户意图
     *
     * @param userMessage 用户原始输入
     * @return 目标 Agent 类型
     */
    AgentType recognize(String userMessage);
}
