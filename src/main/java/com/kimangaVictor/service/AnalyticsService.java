package com.kimangaVictor.service;

import com.kimangaVictor.config.PortfolioProperties;
import com.kimangaVictor.domain.AnalyticsEvent;
import com.kimangaVictor.dto.request.AnalyticsEventRequest;
import com.kimangaVictor.dto.response.AnalyticsSummaryResponse;
import com.kimangaVictor.repository.AnalyticsEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Privacy-preserving visitor analytics.
 *
 * <p>Stores no cookies and no raw IP addresses. Visitors are counted via a salted SHA-256 of the
 * client IP truncated to 16 hex characters, which supports distinct-visitor counts without being
 * reversible to an individual.
 */
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    /** Event type recorded when a visitor opens a project link. */
    public static final String PROJECT_CLICK = "project_click";

    private final AnalyticsEventRepository repository;
    private final PortfolioProperties properties;

    @Transactional
    public void record(AnalyticsEventRequest request, String clientKey) {
        AnalyticsEvent event = new AnalyticsEvent();
        event.setType(request.type());
        event.setPath(request.path());
        event.setReferrer(request.referrer());
        event.setTarget(request.target());
        event.setVisitorHash(hashVisitor(clientKey));
        event.setCreatedAt(Instant.now());
        repository.save(event);
    }

    @Transactional(readOnly = true)
    public AnalyticsSummaryResponse summary() {
        Instant dayAgo = Instant.now().minus(Duration.ofDays(1));
        Instant monthAgo = Instant.now().minus(Duration.ofDays(30));

        Map<String, Long> byType = new LinkedHashMap<>();
        repository.countByType().forEach(row -> byType.put((String) row[0], (Long) row[1]));

        List<AnalyticsSummaryResponse.Count> topProjects =
                repository.countByTargetForType(PROJECT_CLICK).stream()
                        .limit(10)
                        .map(row -> new AnalyticsSummaryResponse.Count((String) row[0], (Long) row[1]))
                        .toList();

        return new AnalyticsSummaryResponse(
                repository.count(),
                repository.countByCreatedAtAfter(dayAgo),
                repository.countByCreatedAtAfter(monthAgo),
                repository.countDistinctVisitorsSince(dayAgo),
                repository.countDistinctVisitorsSince(monthAgo),
                byType,
                topProjects);
    }

    private String hashVisitor(String clientKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(properties.analytics().salt().getBytes(StandardCharsets.UTF_8));
            byte[] hash = digest.digest(clientKey.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash).substring(0, 16);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
