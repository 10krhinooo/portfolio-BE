package com.kimangaVictor.controller;

import com.kimangaVictor.dto.request.ContactRequest;
import com.kimangaVictor.exception.ApiExceptions;
import com.kimangaVictor.service.RateLimitService;
import com.kimangaVictor.service.TelegramService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Contact form intake.
 *
 * <p>CORS is handled globally by {@link com.kimangaVictor.config.CorsConfig}. The old
 * {@code @CrossOrigin} annotation here listed the origin with a trailing slash, which never matches
 * a browser Origin header, so it is deliberately gone.
 */
@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
public class ContactController {

    private static final Logger log = LoggerFactory.getLogger(ContactController.class);
    private static final Map<String, String> ACCEPTED = Map.of("status", "sent");

    private final TelegramService telegramService;
    private final RateLimitService rateLimitService;

    @PostMapping
    public ResponseEntity<Map<String, String>> contact(@Valid @RequestBody ContactRequest request,
                                                       HttpServletRequest httpRequest) {
        // Honeypot hit. Return the same success shape a real submission gets so the bot has no
        // signal it was filtered, but notify nobody.
        if (request.isBot()) {
            log.info("Discarded contact submission that tripped the honeypot");
            return ResponseEntity.ok(ACCEPTED);
        }

        String clientKey = RateLimitService.clientKey(httpRequest);
        if (!rateLimitService.allowContact(clientKey)) {
            throw new ApiExceptions.RateLimitedException(
                    "That is a lot of messages in a short time. Please try again later.");
        }

        telegramService.sendMessage(request.name(), request.email(), request.message());
        return ResponseEntity.ok(ACCEPTED);
    }
}
