package com.company.chat.web.controller;

import com.company.chat.web.dto.ChatMessageRequest;
import com.company.chat.web.dto.CreateSessionRequest;
import com.company.chat.web.dto.SessionSummaryResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChatControllerContractTest {

    @Test
    void chatControllerExposesPlannedRestContract() throws Exception {
        Method chatStream = ChatController.class.getMethod("chatStream", ChatMessageRequest.class);
        PostMapping chatMapping = chatStream.getAnnotation(PostMapping.class);
        assertThat(chatMapping.value()).containsExactly("/api/chat/sse");
        assertThat(chatMapping.produces()).containsExactly(MediaType.TEXT_EVENT_STREAM_VALUE);

        Method listSessions = ChatController.class.getMethod("listSessions");
        assertThat(listSessions.getAnnotation(GetMapping.class).value()).containsExactly("/api/sessions");
        assertThat(listSessions.getGenericReturnType().getTypeName())
                .contains(List.class.getTypeName())
                .contains(SessionSummaryResponse.class.getName());

        Method createSession = ChatController.class.getMethod("createSession", CreateSessionRequest.class);
        assertThat(createSession.getAnnotation(PostMapping.class).value()).containsExactly("/api/sessions");

        Method deleteSession = ChatController.class.getMethod("deleteSession", String.class);
        assertThat(deleteSession.getAnnotation(DeleteMapping.class).value()).containsExactly("/api/sessions/{id}");

        Method listMessages = ChatController.class.getMethod("listMessages", String.class);
        assertThat(listMessages.getAnnotation(GetMapping.class).value()).containsExactly("/api/sessions/{id}/messages");
    }
}
