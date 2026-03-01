package com.imad.pulsechat.chat;

import com.imad.pulsechat.chat.dto.MessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ChatRestController {

    private final ChatService chatService;

    @GetMapping("/{conversationId}/messages")
    public Page<MessageResponse> getMessages(
            @PathVariable UUID conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {

        return chatService
                .getConversationMessages(conversationId, page, size)
                .map(chatService::mapToResponse);
    }
}
