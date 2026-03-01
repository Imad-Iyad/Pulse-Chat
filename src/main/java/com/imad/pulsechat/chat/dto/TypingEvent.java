package com.imad.pulsechat.chat.dto;

import lombok.*;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TypingEvent {
    private UUID conversationId;
    private String username;
    private boolean typing; // true = typing, false = stopped
}
