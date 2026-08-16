package com.kimangaVictor.controller;

import com.kimangaVictor.dto.response.AnalyticsSummaryResponse;
import com.kimangaVictor.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Owner-only endpoints. Guarded by {@link com.kimangaVictor.config.ApiKeyFilter}. */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AnalyticsService analyticsService;

    @GetMapping("/analytics")
    public AnalyticsSummaryResponse analytics() {
        return analyticsService.summary();
    }
}
