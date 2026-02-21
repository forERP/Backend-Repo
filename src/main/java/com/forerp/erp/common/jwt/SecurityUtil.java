package com.forerp.erp.common.jwt;

import com.forerp.erp.user.domain.User;
import com.forerp.erp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityUtil {

    private final UserRepository userRepository;

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new IllegalArgumentException("Authentication is required.");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof User principalUser) {
            if (principalUser.getId() != null) {
                return userRepository.findById(principalUser.getId())
                        .orElseThrow(() -> new IllegalArgumentException("User not found."));
            }

            String principalLoginId = principalUser.getLoginId();
            if (principalLoginId != null && !principalLoginId.isBlank()) {
                return userRepository.findByLoginId(principalLoginId)
                        .orElseThrow(() -> new IllegalArgumentException("User not found."));
            }
        }

        String loginId = authentication.getName();
        if (loginId == null || loginId.isBlank()) {
            throw new IllegalArgumentException("Authentication is required.");
        }

        return userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
    }
}