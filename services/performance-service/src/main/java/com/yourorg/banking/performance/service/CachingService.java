package com.yourorg.banking.performance.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.cache.CacheManager;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class CachingService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private CacheManager cacheManager;

    // User Data Caching (10 minutes TTL)
    @Cacheable(value = "user-cache", key = "#userId", unless = "#result == null")
    public Object getUserData(String userId) {
        // This would typically fetch from database
        return fetchUserDataFromDatabase(userId);
    }

    @CacheEvict(value = "user-cache", key = "#userId")
    public void evictUserData(String userId) {
        // Cache eviction handled by annotation
    }

    @CachePut(value = "user-cache", key = "#userId")
    public Object updateUserData(String userId, Object userData) {
        return userData;
    }

    // Account Data Caching (5 minutes TTL)
    @Cacheable(value = "account-cache", key = "#accountId", unless = "#result == null")
    public Object getAccountData(String accountId) {
        return fetchAccountDataFromDatabase(accountId);
    }

    @CacheEvict(value = "account-cache", key = "#accountId")
    public void evictAccountData(String accountId) {
        // Cache eviction handled by annotation
    }

    @CachePut(value = "account-cache", key = "#accountId")
    public Object updateAccountData(String accountId, Object accountData) {
        return accountData;
    }

    // Transaction Data Caching (1 minute TTL)
    @Cacheable(value = "transaction-cache", key = "#transactionId", unless = "#result == null")
    public Object getTransactionData(String transactionId) {
        return fetchTransactionDataFromDatabase(transactionId);
    }

    @CacheEvict(value = "transaction-cache", key = "#transactionId")
    public void evictTransactionData(String transactionId) {
        // Cache eviction handled by annotation
    }

    // Market Data Caching (30 seconds TTL)
    @Cacheable(value = "market-cache", key = "#symbol", unless = "#result == null")
    public Object getMarketData(String symbol) {
        return fetchMarketDataFromExternalAPI(symbol);
    }

    @CacheEvict(value = "market-cache", key = "#symbol")
    public void evictMarketData(String symbol) {
        // Cache eviction handled by annotation
    }

    // Static Data Caching (1 hour TTL)
    @Cacheable(value = "static-cache", key = "#dataType", unless = "#result == null")
    public Object getStaticData(String dataType) {
        return fetchStaticDataFromDatabase(dataType);
    }

    // Multi-level caching with Redis
    public Object getCachedData(String key, String cacheType) {
        String redisKey = cacheType + ":" + key;
        return redisTemplate.opsForValue().get(redisKey);
    }

    public void setCachedData(String key, String cacheType, Object data, Duration ttl) {
        String redisKey = cacheType + ":" + key;
        redisTemplate.opsForValue().set(redisKey, data, ttl);
    }

    public void evictCachedData(String key, String cacheType) {
        String redisKey = cacheType + ":" + key;
        redisTemplate.delete(redisKey);
    }

    // Pattern-based cache eviction
    public void evictCacheByPattern(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    // Cache statistics
    public Map<String, Object> getCacheStatistics() {
        Map<String, Object> stats = new java.util.HashMap<>();
        
        // Get cache names and their statistics
        cacheManager.getCacheNames().forEach(cacheName -> {
            var cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                stats.put(cacheName + "_size", cache.getNativeCache().size());
            }
        });
        
        // Redis statistics
        stats.put("redis_memory_usage", redisTemplate.getConnectionFactory().getConnection().info("memory").get("used_memory"));
        stats.put("redis_connected_clients", redisTemplate.getConnectionFactory().getConnection().info("clients").get("connected_clients"));
        
        return stats;
    }

    // Cache warming
    public void warmCache(String cacheType, List<String> keys) {
        keys.parallelStream().forEach(key -> {
            try {
                switch (cacheType) {
                    case "user":
                        getUserData(key);
                        break;
                    case "account":
                        getAccountData(key);
                        break;
                    case "transaction":
                        getTransactionData(key);
                        break;
                    case "market":
                        getMarketData(key);
                        break;
                    case "static":
                        getStaticData(key);
                        break;
                }
            } catch (Exception e) {
                // Log error but continue warming other keys
                System.err.println("Error warming cache for key " + key + ": " + e.getMessage());
            }
        });
    }

    // Cache preloading based on access patterns
    public void preloadCache(String cacheType, String userId) {
        // Preload related data based on user access patterns
        switch (cacheType) {
            case "user":
                // Preload user's accounts and recent transactions
                getUserData(userId);
                // Additional preloading logic here
                break;
            case "account":
                // Preload account details and recent transactions
                getAccountData(userId);
                break;
        }
    }

    // Cache invalidation strategies
    @Caching(evict = {
        @CacheEvict(value = "user-cache", key = "#userId"),
        @CacheEvict(value = "account-cache", allEntries = true),
        @CacheEvict(value = "transaction-cache", allEntries = true)
    })
    public void invalidateUserRelatedCaches(String userId) {
        // This method will evict multiple caches when user data changes
    }

    // Cache compression
    public void setCompressedData(String key, String cacheType, Object data, Duration ttl) {
        try {
            // Compress data before storing
            byte[] compressedData = compressData(data);
            String redisKey = cacheType + ":compressed:" + key;
            redisTemplate.opsForValue().set(redisKey, compressedData, ttl);
        } catch (Exception e) {
            // Fallback to uncompressed storage
            setCachedData(key, cacheType, data, ttl);
        }
    }

    public Object getCompressedData(String key, String cacheType) {
        try {
            String redisKey = cacheType + ":compressed:" + key;
            byte[] compressedData = (byte[]) redisTemplate.opsForValue().get(redisKey);
            if (compressedData != null) {
                return decompressData(compressedData);
            }
        } catch (Exception e) {
            // Fallback to uncompressed retrieval
            return getCachedData(key, cacheType);
        }
        return null;
    }

    // Cache TTL management
    public void extendCacheTTL(String key, String cacheType, Duration additionalTTL) {
        String redisKey = cacheType + ":" + key;
        Long currentTTL = redisTemplate.getExpire(redisKey, TimeUnit.SECONDS);
        if (currentTTL != null && currentTTL > 0) {
            redisTemplate.expire(redisKey, Duration.ofSeconds(currentTTL + additionalTTL.getSeconds()));
        }
    }

    // Cache health check
    public boolean isCacheHealthy() {
        try {
            redisTemplate.opsForValue().set("health_check", "ok", Duration.ofSeconds(10));
            String result = (String) redisTemplate.opsForValue().get("health_check");
            return "ok".equals(result);
        } catch (Exception e) {
            return false;
        }
    }

    // Private helper methods
    private Object fetchUserDataFromDatabase(String userId) {
        // Simulate database fetch
        return Map.of("userId", userId, "name", "User " + userId, "email", "user" + userId + "@example.com");
    }

    private Object fetchAccountDataFromDatabase(String accountId) {
        // Simulate database fetch
        return Map.of("accountId", accountId, "balance", 1000.00, "type", "CHECKING");
    }

    private Object fetchTransactionDataFromDatabase(String transactionId) {
        // Simulate database fetch
        return Map.of("transactionId", transactionId, "amount", 100.00, "status", "COMPLETED");
    }

    private Object fetchMarketDataFromExternalAPI(String symbol) {
        // Simulate external API fetch
        return Map.of("symbol", symbol, "price", 150.00, "change", 2.50);
    }

    private Object fetchStaticDataFromDatabase(String dataType) {
        // Simulate database fetch
        return Map.of("dataType", dataType, "data", "Static data for " + dataType);
    }

    private byte[] compressData(Object data) {
        // Implement data compression (e.g., using GZIP)
        // This is a placeholder implementation
        return data.toString().getBytes();
    }

    private Object decompressData(byte[] compressedData) {
        // Implement data decompression
        // This is a placeholder implementation
        return new String(compressedData);
    }
}

