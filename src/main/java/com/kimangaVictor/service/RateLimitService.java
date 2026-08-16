package com.kimangaVictor.service;

import com.kimangaVictor.config.PortfolioProperties;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory per-client token buckets.
 *
 * <p>Single-instance only, which is fine for this deployment — the service runs as one Railway
 * container. If it is ever scaled horizontally these buckets must move to Redis.
 */
@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final PortfolioProperties properties;
    private final Map<String, Bucket> contactBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> analyticsBuckets = new ConcurrentHashMap<>();

    public boolean allowContact(String clientKey) {
        return contactBuckets
                .computeIfAbsent(clientKey, k -> newBucket(properties.contact().maxPerHour(), Duration.ofHours(1)))
                .tryConsume(1);
    }

    public boolean allowAnalytics(String clientKey) {
        return analyticsBuckets
                .computeIfAbsent(clientKey, k -> newBucket(properties.analytics().maxPerMin(), Duration.ofMinutes(1)))
                .tryConsume(1);
    }

    private Bucket newBucket(int capacity, Duration refillPeriod) {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(capacity)
                        .refillGreedy(capacity, refillPeriod)
                        .build())
                .build();
    }

    /**
     * Best-effort client identity. Honours {@code X-Forwarded-For} because Railway terminates TLS
     * at a proxy, so {@code getRemoteAddr()} alone would collapse every visitor into one bucket.
     */
    public static String clientKey(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        String remote = request.getRemoteAddr();
        return remote == null ? "unknown" : remote;
    }
}
