package com.yourorg.banking.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import reactor.core.publisher.Mono;

@Configuration
public class RateLimitingConfig {

    @Bean
    public KeyResolver apiKeyResolver() {
        return exchange -> {
            // Try to get API key from header first
            String apiKey = exchange.getRequest().getHeaders().getFirst("X-API-Key");
            if (apiKey != null && !apiKey.isEmpty()) {
                return Mono.just("api_key:" + apiKey);
            }
            
            // Fall back to IP address
            String clientIp = exchange.getRequest().getRemoteAddress() != null 
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                : "unknown";
            return Mono.just("ip:" + clientIp);
        };
    }

    @Bean
    public KeyResolver userResolver() {
        return exchange -> {
            // Try to get user ID from JWT token
            String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                // In a real implementation, you would decode the JWT and extract user ID
                return Mono.just("user:" + "extracted_user_id");
            }
            
            // Fall back to IP address
            String clientIp = exchange.getRequest().getRemoteAddress() != null 
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                : "unknown";
            return Mono.just("ip:" + clientIp);
        };
    }

    @Bean
    public RedisRateLimiter defaultRateLimiter(ReactiveStringRedisTemplate redisTemplate) {
        return new RedisRateLimiter(1000, 3600); // 1000 requests per hour
    }

    @Bean
    public RedisRateLimiter strictRateLimiter(ReactiveStringRedisTemplate redisTemplate) {
        return new RedisRateLimiter(100, 3600); // 100 requests per hour
    }

    @Bean
    public RedisRateLimiter paymentRateLimiter(ReactiveStringRedisTemplate redisTemplate) {
        return new RedisRateLimiter(50, 3600); // 50 requests per hour
    }
}

