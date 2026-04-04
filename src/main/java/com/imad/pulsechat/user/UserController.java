package com.imad.pulsechat.user;

import com.imad.pulsechat.user.dto.UserSearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/search")
    public Page<UserSearchResponse> searchUsers(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,  // start from the page of index zero
            @RequestParam(defaultValue = "10") int size, // every page has 10 items
            Principal principal // presentation about the current user (logged-in user)
    ) {

        return userService.searchUsers(
                query,
                page,
                size,
                principal.getName() // return the username of the current user (logged-in user)
        );
    }

    @GetMapping("/me")
    public User getCurrentUser(Authentication authentication) {
        return (User) authentication.getPrincipal();
    }
}