package com.kimangaVictor.service;

import com.kimangaVictor.exception.ApiExceptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/** Delivers contact form submissions to a Telegram chat. */
@Service
public class TelegramService {

    private static final ZoneId NAIROBI = ZoneId.of("Africa/Nairobi");
    private static final DateTimeFormatter TIMESTAMP =
            DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a z");

    @Value("${telegram.bot-token}")
    private String botToken;

    @Value("${telegram.chat-id}")
    private String chatId;

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendMessage(String name, String email, String message) {
        String timestamp = ZonedDateTime.now(NAIROBI).format(TIMESTAMP);

        // The previous format string ran the message straight into the "Received" line because it
        // was missing a separator, so every notification arrived visually mangled.
        String text = String.format("""
                        📬 *New Portfolio Message*

                        *Name:* %s
                        *Email:* %s

                        *Message:*
                        %s

                        *Received:* %s""",
                escape(sanitize(name)), escape(sanitize(email)),
                escape(sanitize(message)), escape(timestamp));

        String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = Map.of(
                "chat_id", chatId,
                "text", text,
                "parse_mode", "MarkdownV2");

        try {
            restTemplate.postForEntity(url, new HttpEntity<>(body, headers), String.class);
        } catch (RuntimeException e) {
            throw new ApiExceptions.UpstreamException("Could not deliver the message right now.", e);
        }
    }

    /**
     * Drops control characters, which Telegram rejects outright, and collapses long runs of blank
     * lines so a single submission cannot push the rest of the chat off screen.
     */
    private String sanitize(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("[\\p{Cntrl}&&[^\n]]", "")
                .replaceAll("\n{3,}", "\n\n")
                .trim();
    }

    /** MarkdownV2 requires every reserved character to be backslash escaped. */
    private String escape(String text) {
        return text.replaceAll("([_*\\[\\]()~`>#+\\-=|{}.!])", "\\\\$1");
    }
}
