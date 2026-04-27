package com.company.chat.api.llm;

import com.company.chat.api.message.Message;
import java.util.List;
import reactor.core.publisher.Flux;

public interface LlmClient {

    /**
     * 流式对话
     *
     * @param messages 消息列表（含系统提示、历史、当前问题）
     * @return 逐字流式输出
     */
    Flux<String> streamChat(List<Message> messages);
}
