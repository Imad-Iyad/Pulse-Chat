package com.imad.pulsechat.chat;

import com.imad.pulsechat.chat.dto.MessageResponse;
import com.imad.pulsechat.chat.dto.SendMessageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;

    @MessageMapping("/chat.send")
    public void sendMessage(SendMessageRequest request) {

        Message savedMessage = chatService.saveMessage(request);

        MessageResponse response = new MessageResponse(
                savedMessage.getConversation().getId(),
                savedMessage.getSender().getId(),
                savedMessage.getContent(),
                savedMessage.getSentAt()
        );

        messagingTemplate.convertAndSend(
                "/topic/conversation/" + savedMessage.getConversation().getId(),
                response
        );
    }
}
