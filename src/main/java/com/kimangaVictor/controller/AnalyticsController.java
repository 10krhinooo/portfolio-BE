package com.kimangaVictor.controller;

import com.kimangaVictor.dto.request.AnalyticsEventRequest;
import com.kimangaVictor.service.AnalyticsService;
import com.kimangaVictor.service.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final RateLimitService rateLimitService;

    /**
     * Fire and forget. Always answers 202 even when the client is over its limit, because a dropped
     * metric is never worth showing the visitor an error.
     */
    @PostMapping("/event")
    public ResponseEntity<Void> record(@Valid @RequestBody AnalyticsEventRequest request,
                                       HttpServletRequest httpRequest) {
        String clientKey = RateLimitService.clientKey(httpRequest);
        if (rateLimitService.allowAnalytics(clientKey)) {
            analyticsService.record(request, clientKey);
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}
