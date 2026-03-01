package com.imad.pulsechat.chat;

import com.imad.pulsechat.chat.dto.ConversationResponse;
import com.imad.pulsechat.chat.dto.CreateConversationRequest;
import com.imad.pulsechat.chat.dto.MessageResponse;
import com.imad.pulsechat.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.stream.Collectors;

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

    @PostMapping
    public ConversationResponse createConversation(
            @RequestBody CreateConversationRequest request
    ) {

        Conversation conversation =
                chatService.createOrGetPrivateConversation(
                        request.getUser1Id(),
                        request.getUser2Id()
                );

        return new ConversationResponse(
                conversation.getId(),
                conversation.getParticipants()
                        .stream()
                        .map(User::getId)
                        .collect(Collectors.toSet())
        );
    }
}
