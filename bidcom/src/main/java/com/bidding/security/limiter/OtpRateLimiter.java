package com.bidding.security.limiter;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OtpRateLimiter {
    private final Map<String, LocalDateTime> lastRequestTime = new ConcurrentHashMap<>();
    private static final int TIME_WINDOW_SECONDS = 6;

    public boolean isAllowed(String email) {
        LocalDateTime now = LocalDateTime.now();
        if (!lastRequestTime.containsKey(email)) {
            lastRequestTime.put(email, now);
            return true;
        }

        LocalDateTime lastTime = lastRequestTime.get(email);
        if (now.isAfter(lastTime.plusSeconds(TIME_WINDOW_SECONDS))) {
            lastRequestTime.put(email, now);
            return true;
        }

        return false;
    }
}
