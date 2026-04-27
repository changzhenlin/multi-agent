package com.company.chat.api.agent;

import com.company.chat.api.message.Message;
import java.util.List;

/**
 * 会话上下文值对象
 */
public record ChatContext(
        String sessionId,
        String userId,
        List<Message> history
) {
}
