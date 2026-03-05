package com.imad.pulsechat.chat;

import com.imad.pulsechat.chat.dto.ConversationResponse;
import com.imad.pulsechat.chat.dto.MessageResponse;
import com.imad.pulsechat.user.User;
import com.imad.pulsechat.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ChatRestController {

    private final ChatService chatService;
    private final UserRepository userRepository;

    @GetMapping("/messages/{conversationId}")
    public Page<MessageResponse> getMessages(
            @PathVariable UUID conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {

        return chatService
                .getConversationMessages(conversationId, page, size)
                .map(chatService::mapToResponse);
    }

    @PostMapping("/{userId}")
    public ConversationResponse createConversation(
            @PathVariable UUID userId,
            Authentication authentication
    ) {

        String username = (String) authentication.getPrincipal();

        User currentUser = userRepository
                .findByUsername(username)
                .orElseThrow();

        Conversation conversation =
                chatService.createOrGetPrivateConversation(
                        currentUser.getId(),
                        userId
                );

        return new ConversationResponse(
                conversation.getId(),
                conversation.getParticipants()
                        .stream()
                        .map(User::getId)
                        .collect(Collectors.toSet())
        );
    }

    @GetMapping("/api/me")
    public String me(Principal principal) {
        if (principal == null) {
            return "Principal is null";
        }
        return principal.getName();
    }
}
