package com.company.chat.web.store;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface ChatSessionRepository extends JpaRepository<ChatSessionEntity, String> {

    List<ChatSessionEntity> findAllByOrderByUpdatedAtDesc();
}
