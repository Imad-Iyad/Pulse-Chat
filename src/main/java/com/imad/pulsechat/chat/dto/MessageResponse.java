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
public class MessageResponse {
    private UUID conversationId;
    private UUID senderId;
    private String content;
    private LocalDateTime sentAt;
}
