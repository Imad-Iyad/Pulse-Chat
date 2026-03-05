package com.imad.pulsechat.user;

import com.imad.pulsechat.user.dto.UserSearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public Page<UserSearchResponse> searchUsers(String query, int page, int size, String currentUsername) {

        User currentUser =
                userRepository.findByUsername(currentUsername)
                        .orElseThrow();

        Pageable pageable = PageRequest.of(page, size);

        return userRepository
                .findByUsernameContainingIgnoreCaseAndIdNot(
                        query,
                        currentUser.getId(),
                        pageable
                )
                .map(user -> new UserSearchResponse(
                        user.getId(),
                        user.getUsername(),
                        user.getDisplayName(),
                        user.getStatus()
                ));
    }
}