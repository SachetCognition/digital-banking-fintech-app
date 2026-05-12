package com.yourorg.banking.gateway.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import reactor.core.publisher.Mono;

import java.util.Base64;

@Configuration
public class RateLimitingConfig {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Bean
    public KeyResolver apiKeyResolver() {
        return exchange -> {
            String apiKey = exchange.getRequest().getHeaders().getFirst("X-API-Key");
            if (apiKey != null && !apiKey.isEmpty()) {
                return Mono.just("api_key:" + apiKey);
            }
            String clientIp = exchange.getRequest().getRemoteAddress() != null
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                : "unknown";
            return Mono.just("ip:" + clientIp);
        };
    }

    @Bean
    public KeyResolver userResolver() {
        return exchange -> {
            String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    String[] parts = token.split("\\.");
                    String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
                    String sub = MAPPER.readTree(payload).get("sub").asText();
                    return Mono.just("user:" + sub);
                } catch (Exception e) {
                    // fall through to IP-based resolution
                }
            }
            String clientIp = exchange.getRequest().getRemoteAddress() != null
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                : "unknown";
            return Mono.just("ip:" + clientIp);
        };
    }

    @Bean
    public RedisRateLimiter defaultRateLimiter(ReactiveStringRedisTemplate redisTemplate) {
        return new RedisRateLimiter(1000, 3600);
    }

    @Bean
    public RedisRateLimiter strictRateLimiter(ReactiveStringRedisTemplate redisTemplate) {
        return new RedisRateLimiter(100, 3600);
    }

    @Bean
    public RedisRateLimiter paymentRateLimiter(ReactiveStringRedisTemplate redisTemplate) {
        return new RedisRateLimiter(50, 3600);
    }
}
