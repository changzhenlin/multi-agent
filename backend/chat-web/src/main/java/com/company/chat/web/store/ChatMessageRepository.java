package com.company.chat.web.store;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {

    List<ChatMessageEntity> findBySessionIdOrderByCreatedAtAsc(String sessionId);

    List<ChatMessageEntity> findBySessionIdOrderByCreatedAtDesc(String sessionId, Pageable pageable);
}
