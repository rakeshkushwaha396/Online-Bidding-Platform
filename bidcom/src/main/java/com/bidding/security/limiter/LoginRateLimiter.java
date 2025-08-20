package com.bidding.security.limiter;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoginRateLimiter {
    private final Map<String, LocalDateTime> lastAttemptTime = new ConcurrentHashMap<>();
    private static final int TIME_WINDOW_SECONDS = 3;

    public boolean isAllowed(String username) {
        LocalDateTime now = LocalDateTime.now();
        if (!lastAttemptTime.containsKey(username)) {
            lastAttemptTime.put(username, now);
            return true;
        }

        LocalDateTime lastTime = lastAttemptTime.get(username);
        if (now.isAfter(lastTime.plusSeconds(TIME_WINDOW_SECONDS))) {
            lastAttemptTime.put(username, now);
            return true;
        }

        return false;
    }
}
