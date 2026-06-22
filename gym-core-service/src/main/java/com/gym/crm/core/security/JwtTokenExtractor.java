package com.gym.crm.core.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtTokenExtractor {

    public String extract() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            log.warn("No authentication found in SecurityContext, JWT token will not be propagated");
            return null;
        }

        Object credentials = authentication.getCredentials();

        return credentials instanceof String token ? token : null;
    }
}