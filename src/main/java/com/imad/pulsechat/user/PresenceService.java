package com.imad.pulsechat.user;

import com.imad.pulsechat.common.enums.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PresenceService {

    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public void userConnected(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow();

        user.setStatus(UserStatus.ONLINE);
        userRepository.save(user);

        messagingTemplate.convertAndSend(
                "/topic/presence",
                user.getUsername() + " is ONLINE"
        );
    }

    public void userDisconnected(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow();

        user.setStatus(UserStatus.OFFLINE);
        user.setLastSeen(LocalDateTime.now());

        userRepository.save(user);

        messagingTemplate.convertAndSend(
                "/topic/presence",
                user.getUsername() + " is OFFLINE"
        );
    }
}