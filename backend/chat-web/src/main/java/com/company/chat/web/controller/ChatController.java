package com.company.chat.web.controller;

import com.company.chat.web.dto.ChatMessageRequest;
import com.company.chat.web.dto.ChatMessageResponse;
import com.company.chat.web.dto.CreateSessionRequest;
import com.company.chat.web.dto.SessionSummaryResponse;
import com.company.chat.web.orchestrator.ChatOrchestrator;
import com.company.chat.web.store.ConversationStore;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
public class ChatController {

    private final ChatOrchestrator chatOrchestrator;
    private final ConversationStore conversationStore;
    private final ExecutorService sseExecutor = Executors.newVirtualThreadPerTaskExecutor();

    public ChatController(ChatOrchestrator chatOrchestrator, ConversationStore conversationStore) {
        this.chatOrchestrator = chatOrchestrator;
        this.conversationStore = conversationStore;
    }

    @PostMapping(value = "/api/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@RequestBody ChatMessageRequest request) {
        SseEmitter emitter = new SseEmitter(0L);
        sseExecutor.execute(() -> streamResponse(request, emitter));
        return emitter;
    }

    @GetMapping("/api/sessions")
    public List<SessionSummaryResponse> listSessions() {
        return conversationStore.listSessions();
    }

    @PostMapping("/api/sessions")
    public SessionSummaryResponse createSession(@RequestBody CreateSessionRequest request) {
        return conversationStore.createSession(request.userId(), request.title());
    }

    @DeleteMapping("/api/sessions/{id}")
    public void deleteSession(@PathVariable String id) {
        conversationStore.deleteSession(id);
    }

    @GetMapping("/api/sessions/{id}/messages")
    public List<ChatMessageResponse> listMessages(@PathVariable String id) {
        return conversationStore.listMessages(id);
    }

    private void streamResponse(ChatMessageRequest request, SseEmitter emitter) {
        try (var stream = chatOrchestrator.chat(request.sessionId(), request.userId(), request.message())) {
            var iterator = stream.iterator();
            while (iterator.hasNext()) {
                String chunk = iterator.next();
                emitter.send(SseEmitter.event().data(chunk));
            }
            emitter.complete();
        } catch (IOException exception) {
            emitter.completeWithError(exception);
        } catch (Exception exception) {
            emitter.completeWithError(exception);
        }
    }
}
