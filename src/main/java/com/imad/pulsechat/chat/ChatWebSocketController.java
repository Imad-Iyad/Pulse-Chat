package com.imad.pulsechat.chat;

import com.imad.pulsechat.chat.dto.MessageResponse;
import com.imad.pulsechat.chat.dto.SendMessageRequest;
import com.imad.pulsechat.chat.dto.TypingEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;

    @Transactional
    @MessageMapping("/chat.send")
    public void sendMessage(SendMessageRequest request, Principal principal) {

        String username = principal.getName();

        Message savedMessage = chatService.saveMessage(request, username);

        MessageResponse response = new MessageResponse(
                savedMessage.getConversation().getId(),
                savedMessage.getSender().getId(),
                savedMessage.getSender().getUsername(),
                savedMessage.getContent(),
                savedMessage.getSentAt()
        );

        messagingTemplate.convertAndSend(
                "/topic/conversation/" + request.getConversationId(),
                        response
        );
    }

    @MessageMapping("/chat.typing")
    public void typing(TypingEvent request, Principal principal) {

        String username = principal.getName();

        chatService.validateParticipant(
                request.getConversationId(),
                username
        );

        TypingEvent response = new TypingEvent();
        response.setConversationId(request.getConversationId());
        response.setUsername(username);
        response.setTyping(request.isTyping());

        messagingTemplate.convertAndSend(
                "/topic/conversation/" + request.getConversationId() + "/typing",
                        response
        );
    }
}
