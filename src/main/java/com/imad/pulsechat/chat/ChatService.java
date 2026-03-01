package com.imad.pulsechat.chat;

import com.imad.pulsechat.chat.dto.MessageResponse;
import com.imad.pulsechat.chat.dto.SendMessageRequest;
import com.imad.pulsechat.user.User;
import com.imad.pulsechat.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;

    public Message saveMessage(SendMessageRequest request) {

        Conversation conversation = conversationRepository.findById(request.getConversationId())
                .orElseThrow();

        User sender = userRepository.findById(request.getSenderId())
                .orElseThrow();

        Message message = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .content(request.getContent())
                .sentAt(LocalDateTime.now())
                .build();

        return messageRepository.save(message);
    }

    public Page<Message> getConversationMessages(UUID conversationId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        return messageRepository
                .findByConversationIdOrderBySentAtAsc(conversationId, pageable);
    }

    public MessageResponse mapToResponse(Message message) {
        return new MessageResponse(
                message.getConversation().getId(),
                message.getSender().getId(),
                message.getContent(),
                message.getSentAt()
        );
    }
}
