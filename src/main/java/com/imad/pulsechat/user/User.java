package com.imad.pulsechat.user;

import com.imad.pulsechat.common.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String displayName;

    private String profilePictureUrl;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    private LocalDateTime lastSeen;

    private LocalDateTime createdAt;
}
