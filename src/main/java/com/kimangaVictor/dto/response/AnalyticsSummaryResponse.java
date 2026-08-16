package com.kimangaVictor.dto.response;

import java.util.List;
import java.util.Map;

public record AnalyticsSummaryResponse(
        long totalEvents,
        long eventsLast24h,
        long eventsLast30d,
        long visitorsLast24h,
        long visitorsLast30d,
        Map<String, Long> eventsByType,
        List<Count> topProjects
) {
    /** A single label/count pair. A record rather than a Map.Entry so Jackson emits proper keys. */
    public record Count(String key, long count) {
    }
}
