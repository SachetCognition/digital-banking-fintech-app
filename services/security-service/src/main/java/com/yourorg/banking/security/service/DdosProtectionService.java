package com.yourorg.banking.security.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class DdosProtectionService {

    private final RedisTemplate<String, String> redisTemplate;
    private final SecurityEventService securityEventService;
    
    @Value("${app.security.ddos-protection.enabled:true}")
    private boolean ddosProtectionEnabled;
    
    @Value("${app.security.ddos-protection.max-requests-per-minute:100}")
    private int maxRequestsPerMinute;
    
    @Value("${app.security.ddos-protection.max-requests-per-hour:1000}")
    private int maxRequestsPerHour;
    
    @Value("${app.security.ddos-protection.block-duration-minutes:60}")
    private int blockDurationMinutes;
    
    @Value("${app.security.ddos-protection.whitelist-ips:}")
    private Set<String> whitelistIps;

    public DdosProtectionService(RedisTemplate<String, String> redisTemplate, 
                               SecurityEventService securityEventService) {
        this.redisTemplate = redisTemplate;
        this.securityEventService = securityEventService;
    }

    /**
     * Check if request should be blocked due to DDoS protection
     */
    public boolean isRequestBlocked() {
        if (!ddosProtectionEnabled) {
            return false;
        }

        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return false;
        }

        String clientIp = getClientIpAddress(request);
        
        // Check whitelist
        if (whitelistIps.contains(clientIp)) {
            return false;
        }

        // Check if IP is currently blocked
        if (isIpBlocked(clientIp)) {
            return true;
        }

        // Check rate limits
        if (isRateLimitExceeded(clientIp)) {
            blockIp(clientIp);
            return true;
        }

        return false;
    }

    /**
     * Record a request for rate limiting
     */
    public void recordRequest() {
        if (!ddosProtectionEnabled) {
            return;
        }

        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return;
        }

        String clientIp = getClientIpAddress(request);
        
        // Skip whitelisted IPs
        if (whitelistIps.contains(clientIp)) {
            return;
        }

        // Record request for rate limiting
        recordRequestForRateLimit(clientIp);
    }

    private boolean isIpBlocked(String clientIp) {
        String blockKey = "ddos:blocked:" + clientIp;
        return Boolean.TRUE.equals(redisTemplate.hasKey(blockKey));
    }

    private boolean isRateLimitExceeded(String clientIp) {
        // Check per-minute limit
        String minuteKey = "ddos:minute:" + clientIp + ":" + getCurrentMinute();
        Long minuteCount = redisTemplate.opsForValue().increment(minuteKey);
        redisTemplate.expire(minuteKey, Duration.ofMinutes(1));
        
        if (minuteCount > maxRequestsPerMinute) {
            return true;
        }

        // Check per-hour limit
        String hourKey = "ddos:hour:" + clientIp + ":" + getCurrentHour();
        Long hourCount = redisTemplate.opsForValue().increment(hourKey);
        redisTemplate.expire(hourKey, Duration.ofHours(1));
        
        return hourCount > maxRequestsPerHour;
    }

    private void recordRequestForRateLimit(String clientIp) {
        // Record for per-minute tracking
        String minuteKey = "ddos:minute:" + clientIp + ":" + getCurrentMinute();
        redisTemplate.opsForValue().increment(minuteKey);
        redisTemplate.expire(minuteKey, Duration.ofMinutes(1));

        // Record for per-hour tracking
        String hourKey = "ddos:hour:" + clientIp + ":" + getCurrentHour();
        redisTemplate.opsForValue().increment(hourKey);
        redisTemplate.expire(hourKey, Duration.ofHours(1));
    }

    private void blockIp(String clientIp) {
        String blockKey = "ddos:blocked:" + clientIp;
        redisTemplate.opsForValue().set(blockKey, "blocked", Duration.ofMinutes(blockDurationMinutes));
        
        // Log security event
        securityEventService.logSecurityEvent(
            "DDOS_BLOCK",
            "HIGH",
            clientIp,
            "IP blocked due to rate limit exceeded",
            null
        );
    }

    /**
     * Unblock an IP address
     */
    public void unblockIp(String clientIp) {
        String blockKey = "ddos:blocked:" + clientIp;
        redisTemplate.delete(blockKey);
        
        // Log security event
        securityEventService.logSecurityEvent(
            "DDOS_UNBLOCK",
            "MEDIUM",
            clientIp,
            "IP unblocked by administrator",
            null
        );
    }

    /**
     * Get current rate limit status for an IP
     */
    public RateLimitStatus getRateLimitStatus(String clientIp) {
        String minuteKey = "ddos:minute:" + clientIp + ":" + getCurrentMinute();
        String hourKey = "ddos:hour:" + clientIp + ":" + getCurrentHour();
        
        Long minuteCount = redisTemplate.opsForValue().get(minuteKey) != null ? 
            Long.parseLong(redisTemplate.opsForValue().get(minuteKey)) : 0L;
        Long hourCount = redisTemplate.opsForValue().get(hourKey) != null ? 
            Long.parseLong(redisTemplate.opsForValue().get(hourKey)) : 0L;
        
        boolean isBlocked = isIpBlocked(clientIp);
        
        return new RateLimitStatus(
            clientIp,
            minuteCount,
            hourCount,
            maxRequestsPerMinute,
            maxRequestsPerHour,
            isBlocked
        );
    }

    /**
     * Get all blocked IPs
     */
    public Set<String> getBlockedIps() {
        return redisTemplate.keys("ddos:blocked:*").stream()
            .map(key -> key.replace("ddos:blocked:", ""))
            .collect(java.util.stream.Collectors.toSet());
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            return attributes.getRequest();
        } catch (Exception e) {
            return null;
        }
    }

    private String getCurrentMinute() {
        return String.valueOf(Instant.now().getEpochSecond() / 60);
    }

    private String getCurrentHour() {
        return String.valueOf(Instant.now().getEpochSecond() / 3600);
    }

    public static class RateLimitStatus {
        private final String clientIp;
        private final long minuteCount;
        private final long hourCount;
        private final int maxRequestsPerMinute;
        private final int maxRequestsPerHour;
        private final boolean isBlocked;

        public RateLimitStatus(String clientIp, long minuteCount, long hourCount, 
                             int maxRequestsPerMinute, int maxRequestsPerHour, boolean isBlocked) {
            this.clientIp = clientIp;
            this.minuteCount = minuteCount;
            this.hourCount = hourCount;
            this.maxRequestsPerMinute = maxRequestsPerMinute;
            this.maxRequestsPerHour = maxRequestsPerHour;
            this.isBlocked = isBlocked;
        }

        // Getters
        public String getClientIp() { return clientIp; }
        public long getMinuteCount() { return minuteCount; }
        public long getHourCount() { return hourCount; }
        public int getMaxRequestsPerMinute() { return maxRequestsPerMinute; }
        public int getMaxRequestsPerHour() { return maxRequestsPerHour; }
        public boolean isBlocked() { return isBlocked; }
        
        public boolean isMinuteLimitExceeded() {
            return minuteCount > maxRequestsPerMinute;
        }
        
        public boolean isHourLimitExceeded() {
            return hourCount > maxRequestsPerHour;
        }
    }
}

