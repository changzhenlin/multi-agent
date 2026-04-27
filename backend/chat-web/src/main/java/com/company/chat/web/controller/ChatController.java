package com.company.chat.web.controller;

import com.company.chat.web.dto.ChatMessageRequest;
import com.company.chat.web.dto.ChatMessageResponse;
import com.company.chat.web.dto.CreateSessionRequest;
import com.company.chat.web.dto.SessionSummaryResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
public class ChatController {

    @PostMapping(value = "/api/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@RequestBody ChatMessageRequest request) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Chat stream orchestration is not implemented yet");
    }

    @GetMapping("/api/sessions")
    public List<SessionSummaryResponse> listSessions() {
        return List.of();
    }

    @PostMapping("/api/sessions")
    public SessionSummaryResponse createSession(@RequestBody CreateSessionRequest request) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Session creation is not implemented yet");
    }

    @DeleteMapping("/api/sessions/{id}")
    public void deleteSession(@PathVariable String id) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Session deletion is not implemented yet");
    }

    @GetMapping("/api/sessions/{id}/messages")
    public List<ChatMessageResponse> listMessages(@PathVariable String id) {
        return List.of();
    }
}
