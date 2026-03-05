package com.imad.pulsechat.user.dto;

import com.imad.pulsechat.common.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserSearchResponse {

    private UUID id;
    private String username;
    private String displayName;
    private UserStatus status;
}