package com.company.chat.api.message;

/**
 * 对话消息
 */
public record Message(
        String role,
        String content
) {
}
