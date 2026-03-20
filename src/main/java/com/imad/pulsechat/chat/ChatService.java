package com.imad.pulsechat.chat;

import com.imad.pulsechat.chat.dto.ConversationListDto;
import com.imad.pulsechat.chat.dto.MessageResponse;
import com.imad.pulsechat.chat.dto.SendMessageRequest;
import com.imad.pulsechat.common.enums.ConversationType;
import com.imad.pulsechat.common.exception.ForbiddenException;
import com.imad.pulsechat.user.User;
import com.imad.pulsechat.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;

    public Message saveMessage(SendMessageRequest request, String username) {

        Conversation conversation =
                conversationRepository.findById(request.getConversationId())
                        .orElseThrow();

        User sender =
                userRepository.findByUsername(username)
                        .orElseThrow();

        //تحقق إنه المستخدم مشارك في المحادثة
        boolean isParticipant =
                conversation.getParticipants()
                        .stream()
                        .anyMatch(user -> user.getId().equals(sender.getId()));

        if (!isParticipant) {
            throw new ForbiddenException("You are not a participant in this conversation");
        }

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
                message.getSender().getUsername(),
                message.getContent(),
                message.getSentAt()
        );
    }

    public Conversation createOrGetPrivateConversation(UUID user1Id, UUID user2Id) {

        // حاول تلاقي محادثة موجودة
        Optional<Conversation> existing =
                conversationRepository.findPrivateConversation(user1Id, user2Id);

        if (existing.isPresent()) {
            return existing.get();
        }

        // جيب المستخدمين
        User user1 = userRepository.findById(user1Id).orElseThrow();
        User user2 = userRepository.findById(user2Id).orElseThrow();

        // اعمل محادثة جديدة
        Conversation conversation = Conversation.builder()
                .type(ConversationType.PRIVATE)
                .createdAt(LocalDateTime.now())
                .participants(Set.of(user1, user2))
                .build();

        return conversationRepository.save(conversation);
    }

    public void validateParticipant(UUID conversationId, String username) {

        Conversation conversation =
                conversationRepository.findById(conversationId)
                        .orElseThrow();

        User user =
                userRepository.findByUsername(username)
                        .orElseThrow();

        boolean isParticipant =
                conversation.getParticipants()
                        .stream()
                        .anyMatch(p -> p.getId().equals(user.getId()));

        if (!isParticipant) {
            throw new ForbiddenException(
                    "You are not allowed in this conversation"
            );
        }
    }

    public List<ConversationListDto> getUserConversations(UUID userId) {

        List<Conversation> conversations =
                conversationRepository.findAllByUserId(userId);

        return conversations.stream().map(conversation -> {

                    //الطرف الثاني
                    User otherUser = conversation.getParticipants()
                            .stream()
                            .filter(u -> !u.getId().equals(userId))
                            .findFirst()
                            .orElseThrow();

                    //آخر رسالة
                    Optional<Message> lastMessageOpt =
                            messageRepository.findTopByConversationIdOrderBySentAtDesc(conversation.getId());

                    String lastMessage = lastMessageOpt.map(Message::getContent).orElse(null);
                    LocalDateTime lastMessageTime = lastMessageOpt.map(Message::getSentAt).orElse(null);

                    return ConversationListDto.builder()
                            .conversationId(conversation.getId())
                            .otherUserId(otherUser.getId())
                            .otherUsername(otherUser.getUsername())
                            .lastMessage(lastMessage)
                            .lastMessageTime(lastMessageTime)
                            .build();

                })
                .sorted((a, b) -> {
                    if (a.getLastMessageTime() == null) return 1;
                    if (b.getLastMessageTime() == null) return -1;
                    return b.getLastMessageTime().compareTo(a.getLastMessageTime());
                })
                .toList();
    }
}
