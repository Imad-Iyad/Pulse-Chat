package com.imad.pulsechat.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConversationListDto {

    private UUID conversationId;

    private UUID otherUserId;
    private String otherUsername;

    private String lastMessage;
    private LocalDateTime lastMessageTime;
}