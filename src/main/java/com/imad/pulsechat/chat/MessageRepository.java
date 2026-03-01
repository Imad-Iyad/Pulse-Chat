package com.imad.pulsechat.chat;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

    // جلب رسائل محادثة مرتبة من الأقدم للأحدث
    Page<Message> findByConversationIdOrderBySentAtAsc(UUID conversationId, Pageable pageable);

    // آخر رسالة في محادثة
    Optional<Message> findTopByConversationIdOrderBySentAtDesc(UUID conversationId);

}
