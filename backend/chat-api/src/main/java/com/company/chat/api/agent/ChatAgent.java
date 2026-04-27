package com.company.chat.api.agent;

import com.company.chat.api.message.Message;
import java.util.List;
import java.util.stream.Stream;

public interface ChatAgent {

    /**
     * 处理用户消息，返回流式响应
     *
     * @param context 会话上下文（含历史消息）
     * @param userMessage 当前用户输入
     * @return 流式字符串输出
     */
    Stream<String> chat(ChatContext context, String userMessage);
}
