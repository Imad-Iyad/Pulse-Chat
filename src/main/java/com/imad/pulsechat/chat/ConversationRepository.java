package com.imad.pulsechat.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    // تجيب كل المحادثات تبع user معين
    @Query("""
           SELECT c FROM Conversation c
           JOIN c.participants p
           WHERE p.id = :userId
           """)
    List<Conversation> findAllByUserId(@Param("userId") UUID userId);

    @Query("""
       SELECT c FROM Conversation c
       JOIN c.participants p1
       JOIN c.participants p2
       WHERE c.type = 'PRIVATE'
       AND p1.id = :user1
       AND p2.id = :user2
       """)
    Optional<Conversation> findPrivateConversation(UUID user1, UUID user2);
}
